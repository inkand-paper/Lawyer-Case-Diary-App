package com.lawyercasediary.api

import com.lawyercasediary.BuildConfig
import com.lawyercasediary.auth.SessionManager
import com.lawyercasediary.auth.TokenRefreshAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Production-grade Retrofit client with automated logging, authentication,
 * and token rotation. Optimized for law practice data synchronization.
 */
object RetrofitClient {
    
    // Base URL should be updated to your Vercel deployment URL
    private const val BASE_URL = "https://lawyer-case-diary.vercel.app/"

    fun create(sessionManager: SessionManager, cookieJar: SessionCookieJar): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            // BODY-level logging prints full request/response payloads — including
            // login passwords, JWTs, and refresh tokens — to Logcat. That's fine
            // for a debug build on your own device, but a release build is
            // readable via `adb logcat` (or by any log-scraping tool/crash
            // reporter) on any user's phone. This is a legal-records app, so
            // that's not a minor issue. Only NONE in release builds.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                     else HttpLoggingInterceptor.Level.NONE
        }

        // Lazy initializer for ApiService to prevent circular dependency with Authenticator
        lateinit var apiService: ApiService

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(sessionManager))
            .authenticator(TokenRefreshAuthenticator(sessionManager, lazy { apiService }))
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
        return apiService
    }
}
