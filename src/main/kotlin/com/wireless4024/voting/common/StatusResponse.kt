@file:Suppress("unused")

package com.wireless4024.voting.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class StatusResponse<T>(val ok: Boolean, val error: String?, val body: T?)

fun <T> ok(value: T) = StatusResponse(true, null, value)
fun <T> apiError(error: String?) = StatusResponse<T>(false, error, null)