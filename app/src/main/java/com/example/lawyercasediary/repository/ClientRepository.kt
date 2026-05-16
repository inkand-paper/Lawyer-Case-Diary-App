package com.example.lawyercasediary.repository

import com.example.lawyercasediary.api.ApiService
import com.example.lawyercasediary.models.Client

class ClientRepository(private val apiService: ApiService) {
    suspend fun getClients(limit: Int = 100, offset: Int = 0): Result<List<Client>> {
        return try {
            val response = apiService.getClients(limit, offset)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch clients"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
