package com.example.lawyercasediary.di

import android.content.Context
import com.example.lawyercasediary.api.NetworkModule
import com.example.lawyercasediary.auth.SessionManager
import com.example.lawyercasediary.repository.AuthRepository
import com.example.lawyercasediary.repository.CaseRepository
import com.example.lawyercasediary.repository.ClientRepository

/**
 * Enterprise-grade Dependency Injection container for the Lawyer Case Diary app.
 * Ensures single instances of repositories and services across the app lifecycle.
 */
class AppContainer(context: Context) {
    private val apiService = NetworkModule.apiService
    
    val sessionManager = SessionManager(context)
    
    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, sessionManager)
    }
    
    val caseRepository: CaseRepository by lazy {
        CaseRepository(apiService)
    }
    
    val clientRepository: ClientRepository by lazy {
        ClientRepository(apiService)
    }
}
