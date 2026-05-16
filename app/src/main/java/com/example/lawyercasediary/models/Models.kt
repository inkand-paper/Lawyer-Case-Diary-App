package com.example.lawyercasediary.models

import com.google.gson.annotations.SerializedName

// Generic API wrapper
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

// AUTH
data class LoginRequest(
    val email: String,
    @SerializedName("password") val pin: String
)

data class LoginData(
    val id: String,
    val name: String,
    val email: String,
    val token: String,
    val role: String? = "USER",
    val plan: String? = "FREE"
)

// CASES
data class Case(
    val id: String,
    val title: String,
    val caseNumber: String,
    val courtName: String,
    val status: String,
    val createdAt: String,
    val client: Client? = null,
    val hearings: List<Hearing>? = null
)

data class CreateCaseRequest(
    val title: String,
    val caseNumber: String,
    val courtName: String,
    val clientId: String
)

// HEARINGS
data class Hearing(
    val id: String,
    val caseId: String,
    val hearingDate: String,
    val notes: String?,
    val status: String, // PENDING, COMPLETED, ADJOURNED
    val courtRoom: String? = null
)

data class CreateHearingRequest(
    val caseId: String,
    val hearingDate: String,
    val notes: String?,
    val courtRoom: String? = null
)

// CLIENTS
data class Client(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String? = null,
    val casesCount: Int? = 0
)

// CHAMBERS
data class Chamber(
    val id: String,
    val name: String,
    val address: String,
    val members: List<UserProfile>? = null
)

// NOTIFICATIONS
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val read: Boolean,
    val createdAt: String
)

// STATS / ANALYTICS
data class DashboardStats(
    val totalCases: Int,
    val activeCases: Int,
    val totalClients: Int,
    val upcomingHearings: Int,
    val monthlyRevenue: Double? = 0.0
)

// USER / PROFILE
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val plan: String,
    val avatarUrl: String? = null
)

data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null
)
