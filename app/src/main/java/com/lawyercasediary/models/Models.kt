package com.lawyercasediary.models

import com.google.gson.annotations.SerializedName

/**
 * Data Models — 100% synchronized with Prisma schema and backend API contracts.
 * Prisma schema location: E:\Abir\Lawyer Case Diary\prisma\schema.prisma
 */

// ─── GENERIC API WRAPPER ───────────────────────────────────────────────────────

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// ─── AUTH ──────────────────────────────────────────────────────────────────────

/** POST /api/auth/login — loginSchema: email, password */
data class LoginRequest(
    val email: String,
    val password: String
)

/** POST /api/auth/register — registerSchema: name, email, password (min 8) */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

/** Login/Register response: { id, name, email, token } + cookies for refreshToken */
data class AuthResponse(
    val id: String,
    val name: String,
    val email: String,
    val token: String,
    val refreshToken: String? = null,
    val role: String? = null,
    val emailVerified: Boolean? = null
)

/** POST /api/auth/refresh */
data class RefreshRequest(
    val refreshToken: String
)

data class TokenResponse(
    val token: String,
    val refreshToken: String? = null
)

// ─── USER PROFILE ──────────────────────────────────────────────────────────────

/**
 * GET/PATCH /api/me
 * Prisma User: id, name, email, role, plan, emailVerified, chamberId, createdAt
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String = "LAWYER",        // ADMIN | LAWYER
    val plan: String = "ESSENTIAL",     // ESSENTIAL | EXECUTIVE | ULTIMATE
    val emailVerified: Boolean = false,
    val chamberId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** PATCH /api/me — profileUpdateSchema: name?, email? */
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null
)

// ─── CLIENT ────────────────────────────────────────────────────────────────────

/**
 * Prisma Client: id, userId, chamberId?, name, phone?, email?, address?, createdAt, updatedAt
 * Backend returns client with its cases included.
 */
data class Client(
    val id: String,
    val userId: String = "",
    val chamberId: String? = null,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** POST /api/clients — clientSchema: name (min 2), phone?, email?, address? */
data class CreateClientRequest(
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

/** PATCH /api/clients/{id} — clientUpdateSchema: all fields optional */
data class UpdateClientRequest(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

// ─── CASE ──────────────────────────────────────────────────────────────────────

/**
 * Prisma Case: id, userId, chamberId?, clientId, title, caseNumber, courtName,
 *              judgeName?, status (ACTIVE|CLOSED), description?, createdAt, updatedAt
 * Backend includes: client, hearings (on detail endpoint)
 */
data class Case(
    val id: String,
    val userId: String = "",
    val chamberId: String? = null,
    val clientId: String,
    val title: String,
    val caseNumber: String,
    val courtName: String,
    val judgeName: String? = null,
    val status: String = "ACTIVE",      // ACTIVE | CLOSED
    val description: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val client: Client? = null,
    val hearings: List<Hearing>? = null,
    val notes: List<Note>? = null,
    val payments: List<Payment>? = null
)

/**
 * POST /api/cases — caseSchema:
 *   title (min 2), caseNumber (min 1), courtName (min 2),
 *   judgeName?, clientId (required UUID), description?, status? (ACTIVE|CLOSED)
 */
data class CreateCaseRequest(
    val title: String,
    val caseNumber: String,
    val courtName: String,
    val clientId: String,
    val judgeName: String? = null,
    val description: String? = null,
    val status: String? = null
)

/** PUT /api/cases/{id} — caseUpdateSchema: all fields optional */
data class UpdateCaseRequest(
    val title: String? = null,
    val caseNumber: String? = null,
    val courtName: String? = null,
    val clientId: String? = null,
    val judgeName: String? = null,
    val description: String? = null,
    val status: String? = null          // ACTIVE | CLOSED
)

// ─── HEARING ───────────────────────────────────────────────────────────────────

/**
 * Prisma Hearing: id, caseId, hearingDate (DateTime), nextDate (DateTime?),
 *                 notes?, createdAt, updatedAt
 * Backend includes: case (on list endpoint)
 */
data class Hearing(
    val id: String,
    val caseId: String,
    val hearingDate: String,            // ISO 8601 datetime string
    val nextDate: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val case: Case? = null
)

/**
 * POST /api/hearings — hearingSchema:
 *   caseId (min 1), hearingDate (ISO with T), nextDate? (ISO with T), notes?
 */
data class CreateHearingRequest(
    val caseId: String,
    val hearingDate: String,            // Must be ISO with "T": e.g. "2026-05-21T10:00:00.000Z"
    val nextDate: String? = null,
    val notes: String? = null
)

/** PUT /api/hearings/{id} — hearingUpdateSchema: all fields optional */
data class UpdateHearingRequest(
    val caseId: String? = null,
    val hearingDate: String? = null,
    val nextDate: String? = null,
    val notes: String? = null
)

// ─── NOTE ──────────────────────────────────────────────────────────────────────

/** Prisma Note: id, caseId, content, createdAt, updatedAt */
data class Note(
    val id: String,
    val caseId: String,
    val content: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ─── PAYMENT ───────────────────────────────────────────────────────────────────

/** Prisma Payment: id, caseId, amount, paymentDate, method?, status, createdAt, updatedAt */
data class Payment(
    val id: String,
    val caseId: String,
    val amount: Double,
    val paymentDate: String,
    val method: String? = null,
    val status: String = "COMPLETED",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// ─── CHAMBER ───────────────────────────────────────────────────────────────────

/**
 * Prisma Chamber: id, name, ownerId, members (User[]), clients, cases,
 *                 invites (Invitation[]), createdAt, updatedAt
 */
data class Chamber(
    val id: String,
    val name: String,
    val ownerId: String = "",
    val members: List<UserProfile> = emptyList(),
    val invites: List<Invitation> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/** POST /api/chambers — chamberSchema: name (min 2) */
data class CreateChamberRequest(
    val name: String
)

// ─── INVITATION ────────────────────────────────────────────────────────────────

/**
 * Prisma Invitation: id, email, chamberId, role (OWNER|ADMIN|MEMBER),
 *                    status (PENDING|ACCEPTED|DECLINED), createdAt, updatedAt
 * POST /api/chambers/invites — invitationSchema: email, role (ADMIN|MEMBER, default MEMBER)
 */
data class Invitation(
    val id: String,
    val email: String,
    val chamberId: String,
    val role: String = "MEMBER",        // ADMIN | MEMBER
    val status: String = "PENDING",     // PENDING | ACCEPTED | DECLINED
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CreateInvitationRequest(
    val email: String,
    val role: String = "MEMBER"         // ADMIN | MEMBER
)

// ─── NOTIFICATIONS ─────────────────────────────────────────────────────────────

/**
 * GET /api/notifications/upcoming
 * Returns Hearing objects with nested case (title, caseNumber, courtName)
 * filtered to hearings starting within 1 hour.
 */
data class UpcomingHearing(
    val id: String,
    val caseId: String,
    val hearingDate: String,
    val nextDate: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val case: HearingCaseSummary? = null
)

data class HearingCaseSummary(
    val title: String,
    val caseNumber: String,
    val courtName: String
)

// ─── STATS / DASHBOARD ─────────────────────────────────────────────────────────

/**
 * GET /api/stats response shape:
 * { stats: { activeCases, verifiedClients, upcomingHearings, uptime, emailVerified },
 *   recentActions: Case[] }
 */
data class DashboardStats(
    val stats: SummaryStats,
    val recentActions: List<Case> = emptyList()
)

data class SummaryStats(
    val activeCases: Int = 0,
    val verifiedClients: Int = 0,
    val upcomingHearings: Int = 0,
    val uptime: String? = null,
    val emailVerified: Boolean = false
)

// ─── REMINDER ──────────────────────────────────────────────────────────────────

/** Prisma Reminder: id, userId, caseId?, title, remindAt, status, createdAt, updatedAt */
data class Reminder(
    val id: String,
    val userId: String,
    val caseId: String? = null,
    val title: String,
    val remindAt: String,
    val status: String = "PENDING",     // PENDING | SENT | CANCELLED
    val createdAt: String? = null,
    val updatedAt: String? = null
)
