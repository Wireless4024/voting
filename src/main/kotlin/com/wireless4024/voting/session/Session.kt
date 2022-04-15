package com.wireless4024.voting.session

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Writer

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Session<T, V>(val title: T, val choices: Array<V>) : SessionBase {
	val votes: MutableList<ISessionVote<String, V>> = mutableListOf()
	val since = System.currentTimeMillis()

	val attachments: MutableList<String> = mutableListOf()

	override fun strip(): SessionStat<V> {
		return SessionStat(
			this.title.toString(),
			this.choices,
			votes.map { VoteParticipant(it.name, it.timestamp) },
			this.since
		)
	}

	fun vote(choice: ISessionVote<String, V>): Boolean {
		val votes = this.votes
		if (!votes.any { it.name == choice.name }) {
			votes.add(choice)
			return true
		}
		return false
	}
}