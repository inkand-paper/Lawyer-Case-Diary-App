package com.lawyercasediary.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lawyercasediary.models.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lawyer_prefs")

/**
 * Enterprise-grade Session Management using Jetpack DataStore.
 * Ensures encrypted and efficient persistence of lawyer credentials.
 */
class SessionManager(private val context: Context) {
    
    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_ROLE = stringPreferencesKey("user_role")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE] }

    suspend fun saveSession(auth: AuthResponse) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = auth.token
            auth.refreshToken?.let { prefs[REFRESH_TOKEN] = it }
            prefs[USER_ID] = auth.id
            prefs[USER_NAME] = auth.name
            prefs[USER_ROLE] = auth.role ?: "LAWYER"
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
