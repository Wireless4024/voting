package com.wireless4024.voting

import io.atlassian.util.concurrent.Promise
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val safePath = Regex("[&$#@\n\t\\s.\\\\/\\[\\])(+?><%*\\-^]+")

suspend fun<T> Promise<T>.await():T?= suspendCancellableCoroutine {c->
	done(c::resume)
	fail(c::resumeWithException)
	c.invokeOnCancellation { cancel(true) }
}