package com.wireless4024.voting.jira.response

@kotlinx.serialization.Serializable
data class JiraIssueResponse(
	val startAt: Long,
	val maxResults: Long,
	val total: Long,
	val issues: Array<Issue>
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as JiraIssueResponse

		if (startAt != other.startAt) return false
		if (maxResults != other.maxResults) return false
		if (total != other.total) return false
		if (!issues.contentEquals(other.issues)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = startAt.hashCode()
		result = 31 * result + maxResults.hashCode()
		result = 31 * result + total.hashCode()
		result = 31 * result + issues.contentHashCode()
		return result
	}
}

