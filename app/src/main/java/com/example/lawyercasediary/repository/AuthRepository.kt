package com.example.lawyercasediary.repository

import com.example.lawyercasediary.api.ApiService
import com.example.lawyercasediary.auth.SessionManager
import com.example.lawyercasediary.models.LoginRequest

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(email: String, pin: String): Result<Boolean> {
        return try {
            val response = apiService.login(LoginRequest(email, pin))
            val body = response.body()
            
            if (response.isSuccessful && body != null && body.success) {
                val data = body.data
                sessionManager.saveSession(data.token, data.email)
                Result.success(true)
            } else {
                val errorMessage = body?.message ?: "Invalid credentials or server error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
    }

    suspend fun isLoggedIn(): Boolean {
        return sessionManager.getToken() != null
    }
}
