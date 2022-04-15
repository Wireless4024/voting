package com.wireless4024.voting.session

data class SessionStat<V>(
	val title: String,
	val choices: Array<V>,
	var voted: List<VoteParticipant>,
	val since: Long
) : SessionBase {
	override fun strip() = this

	override fun toString() = title
}