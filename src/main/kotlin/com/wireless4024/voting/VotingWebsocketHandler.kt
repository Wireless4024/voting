package com.wireless4024.voting

import com.fasterxml.jackson.databind.ObjectMapper
import com.wireless4024.voting.session.SessionStat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.reactor.asFlux
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono

@Component
class VotingWebsocketHandler : WebSocketHandler {

	private val json: ObjectMapper = ObjectMapper()
	private val channel = MutableSharedFlow<String>()

	suspend fun boardcast(session: SessionStat<Int>?) {
		val msg = if (session == null) "null" else json.writeValueAsString(session)
		channel.emit(msg ?: "null")
	}

	suspend fun boardcast(raw: String) {
		channel.emit( json.writeValueAsString(raw))
	}

	override fun handle(webSocketSession: WebSocketSession): Mono<Void> {
		val flux = this.channel.asSharedFlow().asFlux()
		return webSocketSession.send(
			flux.map { webSocketSession.textMessage(it ?: return@map null) }
		)
	}
}