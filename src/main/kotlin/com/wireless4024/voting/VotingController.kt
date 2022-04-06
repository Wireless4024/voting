package com.wireless4024.voting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.Writer
import java.net.URI

data class NewSession(val title: String, val have_choice: Boolean?, val choices: String)

data class SessionVote(val name: String, val choice: String)
open class Empty {
	open fun strip() = this
}

data class Session(val title: String, val choices: Array<String>, val votes: MutableList<SessionVote>) : Empty() {
	override fun strip(): Empty {
		return SessionStat(this.title, this.choices, votes.size)
	}

	fun summary(writer: Writer) {
		val counter = mutableMapOf<String, Int>()
		votes.forEach {
			counter.compute(it.choice) { k, v ->
				(v ?: 0) + 1
			}
		}
		val list = counter.entries.toMutableList()
		list.sortByDescending { it.value }
		writer.write("No.\tChoice\n- - - -\n")
		list.forEach {
			writer.write(it.value.toString())
			writer.write("\t")
			writer.write(it.key)
			writer.write("\n")
		}
	}
}

data class SessionStat(val title: String, val choices: Array<String>, val voted: Int) : Empty()

data class VoteData(val name: String, val choice: String)

@Suppress("unused")
@RestController
class VotingController {
	init {
		File("votes").mkdirs()
	}

	var currentSession: Session? = null

	@PostMapping("/create_new")
	fun createSession(@ModelAttribute form: NewSession, resp: ServerHttpResponse) {
		currentSession = when (form.have_choice) {
			true -> Session(
				form.title, form.choices.split("\n")
					.asSequence()
					.map(String::trim)
					.filter(String::isNotEmpty)
					.toList()
					.toTypedArray(), mutableListOf()
			)
			else -> Session(form.title, arrayOf(), mutableListOf())
		}
		resp.redirect("session.html")
	}

	private fun ServerHttpResponse.redirect(url: String) {
		statusCode = HttpStatus.SEE_OTHER
		headers.location = URI.create(url)
	}

	private val safePath = Regex("[&$#@\n\t\\s.\\\\/\\[\\])(+?><%*\\-^]+")

	@GetMapping("/end")
	suspend fun endMapping(resp: ServerHttpResponse) {
		val current = currentSession
		if (current != null && current.votes.isNotEmpty()) {
			val name = current.title.replace(safePath, "-")
			withContext(Dispatchers.IO) {
				File("votes/${name}.txt")
					.outputStream()
					.bufferedWriter()
					.use {
						it.write("= ")
						it.write(current.title)
						it.write(" =\n")

						if (current.choices.isNotEmpty()) {
							current.choices.forEach { choice ->
								it.write(choice)
								it.write("\n")
							}
						}
						it.write("- - - -\n")
						current.votes.forEach { vote ->
							it.write(vote.name)
							it.write(" = ")
							it.write(vote.choice)
							it.write("\n")
						}
						it.write("- - - -\n")
						current.summary(it)
					}
			}
			currentSession = null
			return resp.redirect("summary.html?name=$name")
		}
		currentSession = null
		resp.redirect("new_session.html")
	}

	@GetMapping("/current")
	fun current() = currentSession?.strip() ?: Empty()

	@GetMapping("/summary/{name}")
	suspend fun summary(@PathVariable("name") name: String, resp: ServerHttpResponse): String {
		val filename = name.replace(safePath,"-")
		val file = File("votes/", "$filename.txt")
		return if (file.exists()) {
			resp.headers.contentType = MediaType.TEXT_PLAIN
			withContext(Dispatchers.IO) {
				file.readText()
			}
		} else {
			resp.statusCode = HttpStatus.NOT_FOUND
			""
		}
	}

	@PostMapping("/vote")
	fun vote(@ModelAttribute form: SessionVote, resp: ServerHttpResponse) {
		val current = currentSession ?: return resp.redirect("vote.html")

		if (form.choice.isNotBlank() && form.name.isNotBlank()) {
			if (!current.votes.any { it.name == form.name })
				current.votes.add(form)
		}
		resp.redirect("vote.html")
	}
}