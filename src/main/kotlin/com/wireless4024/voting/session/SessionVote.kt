package com.wireless4024.voting.session

data class SessionVote(override val name: String, override val choice: Int) : ISessionVote<String, Int> {
	override val timestamp: Long = System.currentTimeMillis()
}