package com.lawyercasediary.api

import com.lawyercasediary.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Service — matches exactly the Next.js backend routes.
 * Base URL: https://lawyer-case-diary.vercel.app/
 *
 * Route map (from E:\Abir\Lawyer Case Diary\src\app\api):
 *   POST   api/auth/login
 *   POST   api/auth/register
 *   POST   api/auth/refresh
 *   POST   api/auth/logout
 *   GET    api/me
 *   PATCH  api/me
 *   GET    api/cases?search=&status=
 *   POST   api/cases
 *   GET    api/cases/{id}
 *   PUT    api/cases/{id}
 *   DELETE api/cases/{id}
 *   GET    api/hearings?date=&caseId=
 *   POST   api/hearings
 *   GET    api/hearings/{id}
 *   PUT    api/hearings/{id}
 *   DELETE api/hearings/{id}
 *   GET    api/clients
 *   POST   api/clients
 *   GET    api/clients/{id}
 *   PUT    api/clients/{id}
 *   DELETE api/clients/{id}
 *   GET    api/chambers
 *   POST   api/chambers
 *   GET    api/chambers/invites
 *   POST   api/chambers/invites
 *   DELETE api/chambers/invites/{id}
 *   GET    api/notifications/upcoming
 *   GET    api/stats
 */
interface ApiService {

    // ─── AUTH ─────────────────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<ApiResponse<TokenResponse>>

    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // ─── PROFILE ──────────────────────────────────────────────────────────────

    /** GET /api/me → { id, name, email, role, plan, emailVerified, createdAt } */
    @GET("api/me")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>

    /** PATCH /api/me → { name?, email? } */
    @PATCH("api/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserProfile>>

    // ─── CASES ────────────────────────────────────────────────────────────────

    /** GET /api/cases?search=&status= → List<Case> with client included */
    @GET("api/cases")
    suspend fun getCases(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<Case>>>

    /** POST /api/cases → Case. Body: CreateCaseRequest */
    @POST("api/cases")
    suspend fun createCase(@Body request: CreateCaseRequest): Response<ApiResponse<Case>>

    /** GET /api/cases/{id} → Case with hearings, notes, payments included */
    @GET("api/cases/{id}")
    suspend fun getCaseDetail(@Path("id") id: String): Response<ApiResponse<Case>>

    /** PUT /api/cases/{id} → Case */
    @PUT("api/cases/{id}")
    suspend fun updateCase(
        @Path("id") id: String,
        @Body request: UpdateCaseRequest
    ): Response<ApiResponse<Case>>

    /** DELETE /api/cases/{id} */
    @DELETE("api/cases/{id}")
    suspend fun deleteCase(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ─── HEARINGS ─────────────────────────────────────────────────────────────

    /** GET /api/hearings?date=&caseId= → List<Hearing> with case included */
    @GET("api/hearings")
    suspend fun getHearings(
        @Query("date") date: String? = null,
        @Query("caseId") caseId: String? = null
    ): Response<ApiResponse<List<Hearing>>>

    /** POST /api/hearings → Hearing. Body: CreateHearingRequest (hearingDate must include "T") */
    @POST("api/hearings")
    suspend fun createHearing(@Body request: CreateHearingRequest): Response<ApiResponse<Hearing>>

    /** GET /api/hearings/{id} → Hearing */
    @GET("api/hearings/{id}")
    suspend fun getHearingDetail(@Path("id") id: String): Response<ApiResponse<Hearing>>

    /** PUT /api/hearings/{id} → Hearing */
    @PUT("api/hearings/{id}")
    suspend fun updateHearing(
        @Path("id") id: String,
        @Body request: UpdateHearingRequest
    ): Response<ApiResponse<Hearing>>

    /** DELETE /api/hearings/{id} */
    @DELETE("api/hearings/{id}")
    suspend fun deleteHearing(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ─── CLIENTS ──────────────────────────────────────────────────────────────

    /** GET /api/clients → List<Client> */
    @GET("api/clients")
    suspend fun getClients(): Response<ApiResponse<List<Client>>>

    /** POST /api/clients → Client. Body: CreateClientRequest (name required, others optional) */
    @POST("api/clients")
    suspend fun createClient(@Body request: CreateClientRequest): Response<ApiResponse<Client>>

    /** GET /api/clients/{id} → Client */
    @GET("api/clients/{id}")
    suspend fun getClientDetail(@Path("id") id: String): Response<ApiResponse<Client>>

    /** PUT /api/clients/{id} → Client */
    @PUT("api/clients/{id}")
    suspend fun updateClient(
        @Path("id") id: String,
        @Body request: UpdateClientRequest
    ): Response<ApiResponse<Client>>

    /** DELETE /api/clients/{id} */
    @DELETE("api/clients/{id}")
    suspend fun deleteClient(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ─── CHAMBERS ─────────────────────────────────────────────────────────────

    /** GET /api/chambers → Chamber or null (if not in a chamber) */
    @GET("api/chambers")
    suspend fun getChamber(): Response<ApiResponse<Chamber?>>

    /** POST /api/chambers → Chamber. Requires ULTIMATE plan or ADMIN role. Body: { name } */
    @POST("api/chambers")
    suspend fun createChamber(@Body request: CreateChamberRequest): Response<ApiResponse<Chamber>>

    /** GET /api/chambers/invites → List<Invitation> */
    @GET("api/chambers/invites")
    suspend fun getChamberInvites(): Response<ApiResponse<List<Invitation>>>

    /** POST /api/chambers/invites → Invitation. Body: { email, role } */
    @POST("api/chambers/invites")
    suspend fun sendInvite(@Body request: CreateInvitationRequest): Response<ApiResponse<Invitation>>

    /** DELETE /api/chambers/invites/{id} */
    @DELETE("api/chambers/invites/{id}")
    suspend fun deleteInvite(@Path("id") id: String): Response<ApiResponse<Unit>>

    // ─── NOTIFICATIONS ────────────────────────────────────────────────────────

    /** GET /api/notifications/upcoming → List<UpcomingHearing> (hearings within next 1 hour) */
    @GET("api/notifications/upcoming")
    suspend fun getUpcomingNotifications(): Response<ApiResponse<List<UpcomingHearing>>>

    /** GET /api/reminders → List<Reminder> (system-generated alerts for hearings) */
    @GET("api/reminders")
    suspend fun getReminders(): Response<ApiResponse<List<Reminder>>>

    // ─── STATS / DASHBOARD ────────────────────────────────────────────────────

    /**
     * GET /api/stats →
     * { stats: { activeCases, verifiedClients, upcomingHearings, uptime, emailVerified },
     *   recentActions: Case[] }
     */
    @GET("api/stats")
    suspend fun getStats(): Response<ApiResponse<DashboardStats>>
}
