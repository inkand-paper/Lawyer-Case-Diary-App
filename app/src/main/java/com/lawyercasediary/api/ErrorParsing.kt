package com.lawyercasediary.api

import com.google.gson.Gson

private val errorBodyGson = Gson()

private data class ApiErrorEnvelope(val error: ApiErrorDetail?)
private data class ApiErrorDetail(val code: String?, val message: String?)

/**
 * Extracts the real backend error message from a failed Retrofit response.
 *
 * Retrofit only deserializes `response.body()` for 2xx responses — for any
 * error status, `body()` is null, so `response.body()?.message` is *always*
 * null on failures regardless of what the backend actually sent. The real
 * message lives in `errorBody()`, shaped as {success:false, error:{code,
 * message}} (see src/lib/api-response.ts on the backend). Every repository
 * in this app was falling back to `response.message()` — the generic HTTP
 * reason phrase like "Bad Request" — instead of the specific, useful
 * message the backend actually wrote (e.g. "Invalid credentials provided.",
 * "This reset link is invalid or has expired.").
 *
 * NOTE: errorBody() can only be read once (it's a single-use stream), so
 * call this at most once per Response.
 */
fun retrofit2.Response<*>.parsedErrorMessage(default: String): String {
    val raw = try {
        errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    if (!raw.isNullOrBlank()) {
        try {
            val parsed = errorBodyGson.fromJson(raw, ApiErrorEnvelope::class.java)
            val msg = parsed?.error?.message
            if (!msg.isNullOrBlank()) return msg
        } catch (_: Exception) {
            // Not JSON, or didn't match the expected shape — fall through.
        }
    }
    return default
}
