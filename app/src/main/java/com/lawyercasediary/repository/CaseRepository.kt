package com.lawyercasediary.repository

import com.lawyercasediary.api.ApiService
import com.lawyercasediary.api.parsedErrorMessage
import com.lawyercasediary.models.*

class CaseRepository(private val apiService: ApiService) {

    suspend fun getCases(search: String? = null, status: String? = null): ApiResult<List<Case>> {
        return try {
            val response = apiService.getCases(search, status)
            if (response.isSuccessful && response.body()?.success == true)
                ApiResult.Success(response.body()!!.data ?: emptyList())
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun createCase(request: CreateCaseRequest): ApiResult<Case> {
        return try {
            val response = apiService.createCase(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()!!.data != null)
                ApiResult.Success(response.body()!!.data!!)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun getCaseDetail(id: String): ApiResult<Case> {
        return try {
            val response = apiService.getCaseDetail(id)
            if (response.isSuccessful && response.body()?.success == true && response.body()!!.data != null)
                ApiResult.Success(response.body()!!.data!!)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun updateCase(id: String, request: UpdateCaseRequest): ApiResult<Case> {
        return try {
            val response = apiService.updateCase(id, request)
            if (response.isSuccessful && response.body()?.success == true && response.body()!!.data != null)
                ApiResult.Success(response.body()!!.data!!)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }

    suspend fun deleteCase(id: String): ApiResult<Unit> {
        return try {
            val response = apiService.deleteCase(id)
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(response.code(), response.parsedErrorMessage(response.message()))
        } catch (e: Exception) { ApiResult.Error(-1, e.message ?: "Network Error") }
    }
}
