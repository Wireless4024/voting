package com.wireless4024.voting.session

interface ISessionVote<K, V> {
	val timestamp: Long
	val name: K
	val choice: V
}