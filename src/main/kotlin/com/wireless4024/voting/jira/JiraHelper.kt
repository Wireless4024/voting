package com.wireless4024.voting.jira

import com.wireless4024.voting.jira.response.JiraIssueResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

object JiraHelper {
	val client = HttpClient(CIO) {
		install(ContentNegotiation) {
			json(Json {
				ignoreUnknownKeys = true
			})
		}
	}

	private fun createAuth(username: String, password: String): String {
		return buildString {
			append("Basic ")
			val encoder = Base64.getEncoder()
			append(encoder.encodeToString("$username:$password".toByteArray()))
		}
	}

	private fun buildUrl(base: String, path: String, vararg args: Pair<String, String>): String {
		return buildString {
			append(base)
			if (!base.endsWith('/'))
				append('/')
			append(path)
			for (arg in args) {
				val (key, value) = arg
				append("&")
				append(key)
				append("=")
				append(URLEncoder.encode(value, StandardCharsets.UTF_8))
			}
		}
	}

	suspend fun loadIssues(
		url: String,
		username: String,
		password: String,
		query: String,
		maxResults: Long = 50,
		startAt: Long = 0
	): JiraIssueResponse {
		return client.get(
			buildUrl(
				url,
				"rest/api/2/search?fields=key,summary",
				"jql" to query,
				"maxResults" to maxResults.toString(),
				"startAt" to startAt.toString()
			)
		) {
			headers {
				append("Authorization", createAuth(username, password))
			}
		}.body()
	}
}

