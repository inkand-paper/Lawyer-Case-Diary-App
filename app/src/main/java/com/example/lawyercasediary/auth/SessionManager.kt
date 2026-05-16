package com.example.lawyercasediary.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class SessionManager(private val context: Context) {
    companion object {
        private val TOKEN = stringPreferencesKey("auth_token")
        private val USER_EMAIL = stringPreferencesKey("user_email")
    }

    suspend fun saveSession(token: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN] = token
            prefs[USER_EMAIL] = email
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[TOKEN] }.first()
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
