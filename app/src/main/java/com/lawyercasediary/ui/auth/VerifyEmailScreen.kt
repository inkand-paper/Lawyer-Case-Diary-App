package com.lawyercasediary.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawyercasediary.models.ApiResult
import com.lawyercasediary.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class VerifyUiState(
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null,
    val verified: Boolean = false
)

class VerifyViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(VerifyUiState())
    val uiState: StateFlow<VerifyUiState> = _uiState

    fun verify(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true, error = null, infoMessage = null)
            when (val result = repository.verifyEmail(email, code)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isVerifying = false, verified = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isVerifying = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun resend(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResending = true, error = null, infoMessage = null)
            when (val result = repository.resendVerification(email)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isResending = false,
                        infoMessage = "A fresh code has been sent to $email."
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isResending = false, error = result.message)
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    email: String,
    viewModel: VerifyViewModel,
    onVerified: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Verify Your Account") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uiState.verified) {
                Text(
                    "Account verified",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "You're all set. Please log in to continue.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onVerified, modifier = Modifier.fillMaxWidth()) {
                    Text("Continue to Login")
                }
            } else {
                Text(
                    "We've sent a 6-digit code to $email. It expires in 24 hours.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
                    label = { Text("6-digit code") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
                uiState.infoMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { if (code.length == 6) viewModel.verify(email, code) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isVerifying && code.length == 6
                ) {
                    if (uiState.isVerifying) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Verify Account")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.resend(email) },
                    enabled = !uiState.isResending
                ) {
                    Text(if (uiState.isResending) "Sending..." else "Resend Code")
                }
            }
        }
    }
}
