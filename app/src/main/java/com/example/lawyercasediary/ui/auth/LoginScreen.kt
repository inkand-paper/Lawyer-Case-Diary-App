package com.example.lawyercasediary.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lawyer Case Diary", style = MaterialTheme.typography.headlineLarge)
            Text("Secure Counsel Access", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Lawyer ID / Email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("Security PIN") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (state is AuthState.Error) {
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.login(email, pin) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is AuthState.Loading
            ) {
                if (state is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Secure Login")
                }
            }
        }
    }
}
