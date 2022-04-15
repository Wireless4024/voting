package com.wireless4024.voting

import com.fasterxml.jackson.databind.ObjectMapper
import com.wireless4024.voting.common.StatusResponse
import com.wireless4024.voting.common.apiError
import com.wireless4024.voting.common.ok
import com.wireless4024.voting.jira.JiraIssue
import com.wireless4024.voting.jira.JiraStore
import com.wireless4024.voting.jira.TIME_CHOICES
import com.wireless4024.voting.session.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import java.io.File
import java.net.URI

typealias JiraVoteSession = Session<JiraIssue, Int>?

var currentSession: JiraVoteSession = null

@Suppress("unused")
@RestController
class VotingController {
	var logger: Logger = LoggerFactory.getLogger(VotingController::class.java)

	private val json: ObjectMapper = ObjectMapper()

	@Value("\${voting.access-token}")
	var accessToken = ""

	@Autowired
	private lateinit var voteBoardcaster: VotingWebsocketHandler

	init {
		File("votes").mkdirs()
	}

	@PostMapping("/create_new")
	suspend fun createSession(
		@RequestParam("token") token: String,
		@ModelAttribute form: NewSession,
		resp: ServerHttpResponse
	): StatusResponse<SessionBase> {
		if (token != accessToken) return apiError("")

		val issue = JiraStore[form.key] ?: return apiError("Issue unavailable")
		val newSession = Session(issue, TIME_CHOICES)
		currentSession = newSession
		voteBoardcaster.boardcast(newSession.strip())
		return ok(newSession)
	}

	private fun ServerHttpResponse.redirect(url: String) {
		statusCode = HttpStatus.SEE_OTHER
		headers.location = URI.create(url)
	}

	private suspend fun getFileFor(folder: String, name: String, suffix: String): File {
		return withContext(Dispatchers.IO) {
			var counter = 0
			var file: File
			do {
				file = File(buildString {
					append(folder)
					append('/')
					append(name)
					if (counter != 0)
						append('-').append(counter)
					append(suffix)
					counter += 1
				})
			} while (file.exists())
			file
		}
	}

	@GetMapping("/end")
	suspend fun endMapping(@RequestParam("token") token: String, resp: ServerHttpResponse): StatusResponse<String> {
		if (token != accessToken) return apiError("")

		val current = currentSession
		if (current != null && current.votes.isNotEmpty()) {
			val output = getFileFor("votes", current.title.key, ".json")
			withContext(Dispatchers.IO) {
				output.outputStream().bufferedWriter().use {
					json.writerWithDefaultPrettyPrinter().writeValue(it, current)
				}
			}
			currentSession = null
			val outputName = output.name
			voteBoardcaster.boardcast(output.nameWithoutExtension)
			voteBoardcaster.boardcast(null)
			return ok(outputName)

		}
		currentSession = null
		voteBoardcaster.boardcast(null)
		return apiError("This session hasn't been saved")
	}

	@GetMapping("/current_session")
	fun current(@RequestParam("token") token: String): StatusResponse<SessionStat<Int>> {
		return (if (token == accessToken) {
			currentSession?.strip()?.let(::ok)
		} else {
			null
		}) ?: apiError("no session available")
	}

	@GetMapping("/summary/{key}")
	suspend fun summary(
		@PathVariable("key") name: String,
		resp: ServerHttpResponse
	): String {
		val filename = name.replace(safePath, "-")
		val file = File("votes/", "$filename.json")
		return if (file.exists()) {
			resp.headers.contentType = MediaType.APPLICATION_JSON
			withContext(Dispatchers.IO) {
				file.readText()
			}
		} else {
			resp.statusCode = HttpStatus.NOT_FOUND
			resp.headers.contentType = MediaType.APPLICATION_JSON
			"{}"
		}
	}
	@GetMapping("/check-token")
	suspend fun checkToken(
		@RequestParam("token") token: String
	): StatusResponse<Boolean> = ok(token == accessToken)

	@PostMapping("/vote")
	suspend fun vote(@RequestParam("token") token: String, @ModelAttribute form: SessionVote): StatusResponse<Boolean> {
		val session = currentSession
		val current = session ?: return ok(false)

		if (form.choice != 0 && form.name.isNotBlank()) {
			val resp = ok(current.vote(form))
			voteBoardcaster.boardcast(session.strip())
			return resp
		}

		return ok(false)
	}
}