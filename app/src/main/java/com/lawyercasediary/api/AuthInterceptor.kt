package com.lawyercasediary.api

import com.lawyercasediary.auth.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Professional OkHttp Interceptor for injecting Bearer tokens into every request.
 */
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Skip adding token for auth endpoints
        if (request.url.encodedPath.contains("api/auth")) {
            return chain.proceed(request)
        }

        val token = runBlocking { sessionManager.accessToken.first() }
        
        val requestBuilder = request.newBuilder()
        if (!token.isNullOrBlank() && token != "null") {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        val finalRequest = requestBuilder.build()
        return chain.proceed(finalRequest)
    }
}
