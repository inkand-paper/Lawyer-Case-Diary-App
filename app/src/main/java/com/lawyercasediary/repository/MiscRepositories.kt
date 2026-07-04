package com.lawyercasediary.repository

import com.lawyercasediary.api.ApiService
import com.lawyercasediary.api.parsedErrorMessage
import com.lawyercasediary.models.*

// ─── Helper to safely extract body data ───────────────────────────────────────
private fun <T> safeExtract(
    response: retrofit2.Response<ApiResponse<T>>,
    fallback: String
): ApiResult<T> {
    return if (response.isSuccessful && response.body()?.success == true) {
        val data = response.body()!!.data
        if (data != null) ApiResult.Success(data)
        else ApiResult.Error(response.code(), "Empty response from server.")
    } else {
        ApiResult.Error(response.code(), response.parsedErrorMessage(fallback))
    }
}

// ─── HEARING REPOSITORY ───────────────────────────────────────────────────────

class HearingRepository(private val apiService: ApiService) {

    suspend fun getHearings(date: String? = null, caseId: String? = null): ApiResult<List<Hearing>> {
        return try {
            val response = apiService.getHearings(date, caseId)
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data ?: emptyList())
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun createHearing(request: CreateHearingRequest): ApiResult<Hearing> {
        return try {
            safeExtract(apiService.createHearing(request), "Failed to schedule hearing.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun updateHearing(id: String, request: UpdateHearingRequest): ApiResult<Hearing> {
        return try {
            safeExtract(apiService.updateHearing(id, request), "Failed to update hearing.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun deleteHearing(id: String): ApiResult<Unit> {
        return try {
            val response = apiService.deleteHearing(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }
}

// ─── CLIENT REPOSITORY ────────────────────────────────────────────────────────

class ClientRepository(private val apiService: ApiService) {

    suspend fun getClients(): ApiResult<List<Client>> {
        return try {
            val response = apiService.getClients()
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data ?: emptyList())
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun createClient(request: CreateClientRequest): ApiResult<Client> {
        return try {
            safeExtract(apiService.createClient(request), "Failed to create client.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun getClientDetail(id: String): ApiResult<Client> {
        return try {
            safeExtract(apiService.getClientDetail(id), "Failed to load client.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun updateClient(id: String, request: UpdateClientRequest): ApiResult<Client> {
        return try {
            safeExtract(apiService.updateClient(id, request), "Failed to update client.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun deleteClient(id: String): ApiResult<Unit> {
        return try {
            val response = apiService.deleteClient(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }
}

// ─── CHAMBER REPOSITORY ───────────────────────────────────────────────────────

class ChamberRepository(private val apiService: ApiService) {

    suspend fun getChamber(): ApiResult<Chamber?> {
        return try {
            val response = apiService.getChamber()
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun createChamber(request: CreateChamberRequest): ApiResult<Chamber> {
        return try {
            safeExtract(apiService.createChamber(request), "Failed to create chamber.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun getInvites(): ApiResult<List<Invitation>> {
        return try {
            val response = apiService.getChamberInvites()
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data ?: emptyList())
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun sendInvite(request: CreateInvitationRequest): ApiResult<Invitation> {
        return try {
            safeExtract(apiService.sendInvite(request), "Failed to send invite.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun deleteInvite(id: String): ApiResult<Unit> {
        return try {
            val response = apiService.deleteInvite(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun getMessages(): ApiResult<List<ChamberMessage>> {
        return try {
            val response = apiService.getChamberMessages()
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data ?: emptyList())
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun sendMessage(content: String): ApiResult<ChamberMessage> {
        return try {
            safeExtract(apiService.sendChamberMessage(ChamberMessageRequest(content)), "Failed to send message.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }
}

// ─── NOTIFICATION REPOSITORY ──────────────────────────────────────────────────

class NotificationRepository(private val apiService: ApiService) {
    /** Fetches upcoming hearings within the next 1 hour */
    suspend fun getUpcomingNotifications(): ApiResult<List<UpcomingHearing>> {
        return try {
            val response = apiService.getUpcomingNotifications()
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data ?: emptyList())
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }
}

// ─── STATS REPOSITORY ─────────────────────────────────────────────────────────

class StatsRepository(private val apiService: ApiService) {
    suspend fun getDashboardStats(): ApiResult<DashboardStats> {
        return try {
            safeExtract(apiService.getStats(), "Failed to load dashboard stats.")
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }
}

// ─── REMINDER REPOSITORY ──────────────────────────────────────────────────────

class ReminderRepository(private val apiService: ApiService) {
    suspend fun getReminders(): ApiResult<List<Reminder>> {
        return try {
            val response = apiService.getReminders()
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data ?: emptyList())
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }
}

