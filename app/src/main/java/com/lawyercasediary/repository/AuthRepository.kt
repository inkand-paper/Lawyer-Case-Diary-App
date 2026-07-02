package com.lawyercasediary.repository

import com.lawyercasediary.api.ApiService
import com.lawyercasediary.api.parsedErrorMessage
import com.lawyercasediary.auth.SessionManager
import com.lawyercasediary.models.*

/**
 * Enterprise Authentication Repository for managing lawyer identity and sessions.
 */
class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun login(request: LoginRequest): ApiResult<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    sessionManager.saveSession(data)
                    ApiResult.Success(data)
                } else {
                    ApiResult.Error(response.code(), "Empty response from server")
                }
            } else {
                ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Authentication failed")
        }
    }

    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    sessionManager.saveSession(data)
                    ApiResult.Success(data)
                } else {
                    ApiResult.Error(response.code(), "Empty response from server")
                }
            } else {
                ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Registration failed")
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } finally {
            sessionManager.clearSession()
        }
    }

    /**
     * Always resolves to Success from the caller's point of view when the
     * request reaches the server — the backend intentionally returns 200
     * whether or not the email exists, to prevent account enumeration.
     */
    suspend fun forgotPassword(email: String): ApiResult<Unit> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Network error")
        }
    }

    suspend fun resendVerification(email: String): ApiResult<Unit> {
        return try {
            val response = apiService.resendVerification(ResendVerificationRequest(email))
            if (response.isSuccessful && response.body()?.success == true) ApiResult.Success(Unit)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Network error")
        }
    }

    suspend fun verifyEmail(email: String, code: String): ApiResult<Unit> {
        return try {
            val response = apiService.verifyEmail(VerifyEmailRequest(email, code))
            if (response.isSuccessful && response.body()?.success == true) ApiResult.Success(Unit)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Network error")
        }
    }

    suspend fun getProfile(): ApiResult<UserProfile> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    ApiResult.Success(data)
                } else {
                    ApiResult.Error(response.code(), "Profile data missing")
                }
            } else {
                ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Profile sync failed")
        }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): ApiResult<UserProfile> {
        return try {
            val response = apiService.updateProfile(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) ApiResult.Success(data)
                else ApiResult.Error(response.code(), "Empty response")
            } else {
                ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Update failed")
        }
    }
}
