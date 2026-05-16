package com.example.lawyercasediary.repository

import com.example.lawyercasediary.api.ApiService
import com.example.lawyercasediary.models.Case
import com.example.lawyercasediary.models.CreateCaseRequest

class CaseRepository(private val apiService: ApiService) {
    
    suspend fun getCases(limit: Int = 50, offset: Int = 0): Result<List<Case>> {
        return try {
            val response = apiService.getCases(limit, offset)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Network error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCase(title: String, caseNumber: String, courtName: String, clientId: String): Result<Case> {
        return try {
            val request = CreateCaseRequest(title, caseNumber, courtName, clientId)
            val response = apiService.createCase(request)
            val body = response.body()
            if (response.isSuccessful && body != null && body.success) {
                Result.success(body.data)
            } else {
                Result.failure(Exception(body?.message ?: "Save Failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
