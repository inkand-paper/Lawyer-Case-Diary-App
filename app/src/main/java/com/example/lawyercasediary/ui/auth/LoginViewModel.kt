package com.example.lawyercasediary.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawyercasediary.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, pin: String) {
        if (email.isBlank() || pin.isBlank()) {
            _state.value = AuthState.Error("Please enter credentials")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            authRepository.login(email, pin)
                .onSuccess { _state.value = AuthState.Success }
                .onFailure { _state.value = AuthState.Error(it.message ?: "Authentication failed") }
        }
    }
}
