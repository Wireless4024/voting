package com.wireless4024.voting

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.input.FieldInput
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import com.wireless4024.voting.common.StatusResponse
import com.wireless4024.voting.common.apiError
import com.wireless4024.voting.common.ok
import com.wireless4024.voting.jira.IssueStream
import com.wireless4024.voting.jira.JiraIssue
import com.wireless4024.voting.jira.JiraStore
import com.wireless4024.voting.jira.JiraStore.issues
import com.wireless4024.voting.jira.UpdateStoryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.asMono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.net.URI


@RestController
class JiraController {
	var logger: Logger = LoggerFactory.getLogger(JiraController::class.java)
	lateinit var client: JiraRestClient

	@Value("\${voting.jira.url}")
	var jiraUrl = ""

	@Value("\${voting.jira.username}")
	var jiraUsername = ""

	@Value("\${voting.jira.password}")
	var jiraPassword = ""

	@Value("\${voting.jira.point_field}")
	var jiraPointField = ""

	@Value("\${voting.jira.query}")
	var jiraQuery = ""

	@Value("\${voting.access-token}")
	var accessToken = ""

	suspend fun fetchJiraAsync() {
		JiraStore.clear()
		val stream = IssueStream(jiraUrl, jiraUsername, jiraPassword, jiraQuery)
		for (issue in stream.allIssues()) {
			JiraStore.add(issue)
		}
		logger.info("fetch jira successfully")
	}

	@PostMapping("/issue/{key}/update")
	suspend fun setPoint(
		@RequestParam("token") token: String,
		@ModelAttribute form: UpdateStoryPoint,
		@PathVariable("key") key: String
	): Boolean {
		if (token != accessToken) return false
		if (jiraPointField.isBlank()) return false
		client.issueClient.updateIssue(key, IssueInput.createWithFields(FieldInput(jiraPointField, form.point))).await()
		return true
	}

	@Autowired
	fun initJira() {
		GlobalScope.launch {
			listOf(
				async { fetchJiraAsync() },
				async {
					client = AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
						URI.create(jiraUrl),
						jiraUsername,
						jiraPassword
					)
				}).joinAll()
		}.asMono(Dispatchers.IO).block()
	}

	@GetMapping("/issues")
	fun allIssue(@RequestParam("token") token: String): StatusResponse<List<JiraIssue>> {
		if (token != accessToken) return apiError("")
		return ok(issues)
	}

	@GetMapping("/issues/refresh")
	suspend fun refreshIssue(@RequestParam("token") token: String): StatusResponse<Boolean> {
		if (token != accessToken) return apiError("")

		return try {
			fetchJiraAsync()
			ok(true)
		} catch (t: Throwable) {
			apiError(t.message)
		}
	}

	@GetMapping("/issue/{key}")
	fun allIssue(@RequestParam("token") token: String, @PathVariable("key") key: String): StatusResponse<JiraIssue> {
		if (token != accessToken) return apiError("")
		return JiraStore[key]?.let(::ok) ?: apiError("Not available")
	}
}