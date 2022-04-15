import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("org.springframework.boot") version "3.0.0-M2"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.20"
	kotlin("plugin.spring") version "1.6.20"
	kotlin("plugin.serialization") version "1.6.20"
}

group = "com.wireless4024"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://packages.atlassian.com/mvn/maven-atlassian-external/") }
}

val ktor_version = "2.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("com.atlassian.jira:jira-rest-java-client-core:5.2.4")
	implementation("io.atlassian.fugue:fugue:5.0.0")
	implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
	implementation("io.ktor:ktor-client-core:$ktor_version")
	implementation("io.ktor:ktor-client-cio:$ktor_version")
	implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
	implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<BootJar> {
	doFirst {
		println("Building ui stuff (if task fail you you please make sure you have npm installed)")
		Runtime.getRuntime().exec("npm i", null, File("./voting-ui")).apply {
			errorStream.transferTo(System.err)
			inputStream.transferTo(System.out)
		}.waitFor()
		Runtime.getRuntime().exec("npm run build", null, File("./voting-ui")).apply {
			errorStream.transferTo(System.err)
			inputStream.transferTo(System.out)
		}.waitFor()
		File("./voting-ui/public").copyRecursively(File("./src/main/resources/static"),true)
	}
}