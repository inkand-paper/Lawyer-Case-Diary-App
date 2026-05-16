package com.example.lawyercasediary.api

import com.example.lawyercasediary.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // === AUTHENTICATION ===
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @POST("api/auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<ApiResponse<LoginData>>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body token: Map<String, String>): Response<ApiResponse<LoginData>>


    // === PROFILE / ME ===
    @GET("api/me")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>

    @PUT("api/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserProfile>>


    // === CASES ===
    @GET("api/cases")
    suspend fun getCases(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<Case>>>

    @POST("api/cases")
    suspend fun createCase(@Body request: CreateCaseRequest): Response<ApiResponse<Case>>

    @GET("api/cases/{id}")
    suspend fun getCaseDetails(@Path("id") id: String): Response<ApiResponse<Case>>

    @PUT("api/cases/{id}")
    suspend fun updateCase(@Path("id") id: String, @Body data: Map<String, String>): Response<ApiResponse<Case>>

    @DELETE("api/cases/{id}")
    suspend fun deleteCase(@Path("id") id: String): Response<ApiResponse<Unit>>


    // === HEARINGS ===
    @GET("api/hearings")
    suspend fun getHearings(@Query("caseId") caseId: String? = null): Response<ApiResponse<List<Hearing>>>

    @POST("api/hearings")
    suspend fun createHearing(@Body request: CreateHearingRequest): Response<ApiResponse<Hearing>>

    @PUT("api/hearings/{id}")
    suspend fun updateHearing(@Path("id") id: String, @Body data: Map<String, String>): Response<ApiResponse<Hearing>>

    @DELETE("api/hearings/{id}")
    suspend fun deleteHearing(@Path("id") id: String): Response<ApiResponse<Unit>>


    // === CLIENTS ===
    @GET("api/clients")
    suspend fun getClients(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null
    ): Response<ApiResponse<List<Client>>>

    @POST("api/clients")
    suspend fun createClient(@Body client: Client): Response<ApiResponse<Client>>

    @GET("api/clients/{id}")
    suspend fun getClientDetails(@Path("id") id: String): Response<ApiResponse<Client>>

    @DELETE("api/clients/{id}")
    suspend fun deleteClient(@Path("id") id: String): Response<ApiResponse<Unit>>


    // === CHAMBERS ===
    @GET("api/chambers")
    suspend fun getChambers(): Response<ApiResponse<List<Chamber>>>

    @POST("api/chambers")
    suspend fun joinChamber(@Body inviteCode: Map<String, String>): Response<ApiResponse<Chamber>>


    // === STATS & ANALYTICS ===
    @GET("api/stats")
    suspend fun getStats(): Response<ApiResponse<DashboardStats>>


    // === NOTIFICATIONS ===
    @GET("api/notifications")
    suspend fun getNotifications(): Response<ApiResponse<List<Notification>>>

    @POST("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): Response<ApiResponse<Unit>>


    // === ADMIN ===
    @GET("api/admin/users")
    suspend fun adminGetUsers(): Response<ApiResponse<List<UserProfile>>>

    @GET("api/admin/system")
    suspend fun adminGetSystemStatus(): Response<ApiResponse<Map<String, String>>>
}
