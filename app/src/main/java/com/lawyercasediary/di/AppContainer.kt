package com.lawyercasediary.di

import android.content.Context
import com.lawyercasediary.api.RetrofitClient
import com.lawyercasediary.api.SessionCookieJar
import com.lawyercasediary.auth.SessionManager
import com.lawyercasediary.repository.*

/**
 * Manual Dependency Injection Container (Service Locator).
 * Manages singletons and provides clean dependency resolution across the app.
 */
class AppContainer(private val context: Context) {

    val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    val cookieJar: SessionCookieJar by lazy {
        SessionCookieJar()
    }

    private val apiService by lazy {
        RetrofitClient.create(sessionManager, cookieJar)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(apiService, sessionManager)
    }

    val caseRepository: CaseRepository by lazy {
        CaseRepository(apiService)
    }

    val hearingRepository: HearingRepository by lazy {
        HearingRepository(apiService)
    }

    val clientRepository: ClientRepository by lazy {
        ClientRepository(apiService)
    }

    val chamberRepository: ChamberRepository by lazy {
        ChamberRepository(apiService)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(apiService)
    }

    val statsRepository: StatsRepository by lazy {
        StatsRepository(apiService)
    }

    val reminderRepository: ReminderRepository by lazy {
        ReminderRepository(apiService)
    }
}
