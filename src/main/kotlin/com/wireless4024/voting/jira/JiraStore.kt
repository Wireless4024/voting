package com.wireless4024.voting.jira

object JiraStore {
	var issues = mutableListOf<JiraIssue>()
	var issueByKey = mutableMapOf<String, JiraIssue>()

	fun add(issue: JiraIssue) {
		issues.add(issue)
		issueByKey[issue.key] = issue
	}

	operator fun get(key: String) = issueByKey[key]

	fun clear() {
		issues = mutableListOf()
		issueByKey.clear()
	}
}