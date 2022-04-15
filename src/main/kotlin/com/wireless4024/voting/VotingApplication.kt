package com.wireless4024.voting

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import java.io.File

@SpringBootApplication
class VotingApplication {
	@Autowired
	private var webSocketHandler: WebSocketHandler? = null

	@Value("\${voting.origin}")
	var origin = ""

	@Bean
	fun webSocketHandlerMapping(): HandlerMapping {
		val handlerMapping = SimpleUrlHandlerMapping()
		handlerMapping.order = 1
		handlerMapping.urlMap = mutableMapOf("/ws/current_session" to webSocketHandler)
		return handlerMapping
	}

	@Bean
	fun corsConfigurer(): WebFluxConfigurer {
		return object : WebFluxConfigurer {
			override fun addCorsMappings(registry: CorsRegistry) {
				registry.addMapping("/**")
					.allowedOrigins("http://localhost:8080")
					.allowedHeaders("*")
					.apply {
						if (origin.isNotBlank()) allowedOrigins(origin)
					}
			}
		}
	}
}

fun main(args: Array<String>) {
	File("application.properties").let {
		if (!it.exists()) {
			it.outputStream().use {
				VotingApplication::class.java.classLoader
					.getResourceAsStream("application.properties")
					.use { stream -> stream?.transferTo(it) }
			}
		}
	}
	// TODO: discord hook
	runApplication<VotingApplication>(*args)
}
