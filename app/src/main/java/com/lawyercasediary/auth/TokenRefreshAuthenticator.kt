package com.lawyercasediary.auth

import com.lawyercasediary.api.ApiService
import com.lawyercasediary.models.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Advanced OkHttp Authenticator for seamless JWT rotation.
 * Automatically refreshes expired tokens to prevent lawyer logout.
 */
class TokenRefreshAuthenticator(
    private val sessionManager: SessionManager,
    private val apiService: Lazy<ApiService> // Use Lazy to avoid circular dependency
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't try to refresh if we're already on the refresh endpoint (prevents infinite loops)
        if (response.request.url.encodedPath.contains("api/auth/refresh")) {
            return null
        }

        val refreshToken = runBlocking { sessionManager.refreshToken.first() }
        if (refreshToken.isNullOrBlank() || refreshToken == "null") {
            // No valid refresh token, clear session and return null
            runBlocking { sessionManager.clearSession() }
            return null
        }

        // Attempt to refresh the token
        val res = try {
            runBlocking {
                apiService.value.refreshToken(RefreshRequest(refreshToken))
            }
        } catch (e: Exception) {
            return null
        }

        return if (res.isSuccessful && res.body()?.success == true) {
            val tokenData = res.body()?.data
            if (tokenData != null) {
                runBlocking { 
                    sessionManager.updateTokens(
                        tokenData.token, 
                        tokenData.refreshToken ?: refreshToken // Keep old refresh token if not rotated
                    ) 
                }
                
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${tokenData.token}")
                    .build()
            } else {
                runBlocking { sessionManager.clearSession() }
                null
            }
        } else {
            // Refresh failed, logout user
            runBlocking { sessionManager.clearSession() }
            null
        }
    }
}
