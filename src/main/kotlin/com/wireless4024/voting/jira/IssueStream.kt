package com.wireless4024.voting.jira

import com.wireless4024.voting.jira.response.JiraIssueResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

data class IssueStream(
	private val url: String,
	private val username: String,
	private val password: String,
	private val query: String,
	private val maxResults: Long = 50,
	private var cursor: Long = 0
) : Iterator<Deferred<JiraIssueResponse>> {

	var scope: CoroutineScope? = null
	var total = Long.MAX_VALUE
	var last: JiraIssueResponse? = null

	override fun hasNext(): Boolean {
		return cursor < total
	}

	suspend fun asyncNext(): JiraIssueResponse {
		val resp = JiraHelper.loadIssues(url, username, password, query, maxResults, cursor)
		total = resp.total
		cursor += resp.issues.size
		return resp
	}

	override fun next(): Deferred<JiraIssueResponse> {
		if (scope == null) scope = CoroutineScope(Dispatchers.IO)
		return scope!!.async {
			asyncNext()
		}
	}

	suspend fun allIssues(): List<JiraIssue> {
		if (!hasNext()) return emptyList()
		val issues = mutableListOf<JiraIssue>()

		var current = asyncNext()

		while (current.issues.isNotEmpty()) {
			issues.addAll(current.issues.asSequence()
				.map { it.summary?.run { JiraIssue(it.key, this) } }
				.filterNotNull())
			if (hasNext())
				current = asyncNext()
			else break
		}

		return issues
	}
}