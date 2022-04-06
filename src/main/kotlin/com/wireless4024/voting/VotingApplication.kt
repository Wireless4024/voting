package com.wireless4024.voting

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File

@SpringBootApplication
class VotingApplication

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
