package com.wireless4024.voting.jira.response

@kotlinx.serialization.Serializable
data class Issue(val key: String, val fields: MutableMap<String, String>) {
	val summary get() = fields.entries.firstOrNull() { it.key == "summary" }?.value
}