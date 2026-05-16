package com.lawyercasediary.ui.clients

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.ClientRepository
import com.lawyercasediary.ui.components.LoadingSpinner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientDetailViewModel(
    private val repository: ClientRepository,
    private val clientId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _uiState

    init { loadClient() }

    fun loadClient() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getClientDetail(clientId)) {
                is ApiResult.Success<Client> -> {
                    _uiState.value = ClientDetailUiState(client = result.data, isLoading = false)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }
}

data class ClientDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val client: Client? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    viewModel: ClientDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Client Portfolio") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) LoadingSpinner()
            else if (uiState.error != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadClient() }) { Text("Retry") }
                }
            }
            else uiState.client?.let { client ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(client.name, style = MaterialTheme.typography.headlineMedium)
                    Text("Client Portfolio", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    DetailItem(Icons.Default.Phone, "Mobile", client.phone ?: "Not provided")
                    DetailItem(Icons.Default.Email, "Email Address", client.email ?: "Not provided")
                    DetailItem(Icons.Default.Place, "Residential Address", client.address ?: "Not provided")
                }
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
