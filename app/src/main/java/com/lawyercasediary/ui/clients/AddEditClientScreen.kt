package com.lawyercasediary.ui.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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

data class AddEditClientUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = ""
)

class AddEditClientViewModel(
    private val repository: ClientRepository,
    private val clientId: String?
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditClientUiState())
    val uiState: StateFlow<AddEditClientUiState> = _uiState

    init {
        if (clientId != null) loadClient(clientId)
    }

    private fun loadClient(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getClientDetail(id)) {
                is ApiResult.Success -> {
                    val c = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = c.name,
                        phone = c.phone ?: "",
                        email = c.email ?: "",
                        address = c.address ?: ""
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        if (_uiState.value.name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Client name is required.")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val result = if (clientId == null) {
                repository.createClient(CreateClientRequest(
                    name = _uiState.value.name,
                    phone = _uiState.value.phone.ifBlank { null },
                    email = _uiState.value.email.ifBlank { null },
                    address = _uiState.value.address.ifBlank { null }
                ))
            } else {
                repository.updateClient(clientId, UpdateClientRequest(
                    name = _uiState.value.name,
                    phone = _uiState.value.phone.ifBlank { null },
                    email = _uiState.value.email.ifBlank { null },
                    address = _uiState.value.address.ifBlank { null }
                ))
            }
            
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, error = result.message)
                }
                else -> {}
            }
        }
    }

    fun updateName(v: String) { _uiState.value = _uiState.value.copy(name = v) }
    fun updatePhone(v: String) { _uiState.value = _uiState.value.copy(phone = v) }
    fun updateEmail(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun updateAddress(v: String) { _uiState.value = _uiState.value.copy(address = v) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
    viewModel: AddEditClientViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isLoading) "Loading..." else "Client Information") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.onSave(onSuccess) }, enabled = !uiState.isSaving) {
                        if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        else Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingSpinner()
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Client Full Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::updatePhone,
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = viewModel::updateAddress,
                    label = { Text("Physical Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
