package com.wireless4024.voting.jira

data class JiraIssue(val key: String, val summary: String) {
	override fun toString() = buildString {
		append(key)
		append(" ")
		append(summary)
	}
}