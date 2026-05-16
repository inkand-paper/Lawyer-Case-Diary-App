package com.lawyercasediary.ui.cases

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
import com.lawyercasediary.repository.CaseRepository
import com.lawyercasediary.repository.ClientRepository
import com.lawyercasediary.ui.components.LoadingSpinner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ─── UI STATE ─────────────────────────────────────────────────────────────────

data class AddEditCaseUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // Form fields matching caseSchema / caseUpdateSchema
    val title: String = "",
    val caseNumber: String = "",
    val courtName: String = "",
    val judgeName: String = "",
    val description: String = "",
    val status: String = "ACTIVE",      // ACTIVE | CLOSED
    // Client selection
    val selectedClientId: String = "",
    val selectedClientName: String = "",
    val clients: List<Client> = emptyList(),
    val clientsLoading: Boolean = false
)

// ─── VIEW MODEL ───────────────────────────────────────────────────────────────

class AddEditCaseViewModel(
    private val caseRepository: CaseRepository,
    private val clientRepository: ClientRepository,
    private val caseId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCaseUiState())
    val uiState: StateFlow<AddEditCaseUiState> = _uiState

    init {
        loadClients()
        if (caseId != null) loadCase(caseId)
    }

    /** Load all clients for the dropdown */
    private fun loadClients() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(clientsLoading = true)
            when (val result = clientRepository.getClients()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    clients = result.data,
                    clientsLoading = false
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    clientsLoading = false,
                    error = "Could not load clients: ${result.message}"
                )
                else -> {}
            }
        }
    }

    /** Load existing case for editing */
    private fun loadCase(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = caseRepository.getCaseDetail(id)) {
                is ApiResult.Success -> {
                    val c = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = c.title,
                        caseNumber = c.caseNumber,
                        courtName = c.courtName,
                        judgeName = c.judgeName ?: "",
                        description = c.description ?: "",
                        status = c.status,
                        selectedClientId = c.clientId,
                        selectedClientName = c.client?.name ?: ""
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
                else -> {}
            }
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validation
        if (state.title.length < 2) {
            _uiState.value = state.copy(error = "Case title must be at least 2 characters."); return
        }
        if (state.caseNumber.isBlank()) {
            _uiState.value = state.copy(error = "Case reference number is required."); return
        }
        if (state.courtName.length < 2) {
            _uiState.value = state.copy(error = "Court name must be at least 2 characters."); return
        }
        if (state.selectedClientId.isBlank()) {
            _uiState.value = state.copy(error = "Please select a client."); return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val result = if (caseId == null) {
                // CREATE — uses caseSchema
                caseRepository.createCase(
                    CreateCaseRequest(
                        title = state.title.trim(),
                        caseNumber = state.caseNumber.trim(),
                        courtName = state.courtName.trim(),
                        clientId = state.selectedClientId,
                        judgeName = state.judgeName.trim().ifBlank { null },
                        description = state.description.trim().ifBlank { null },
                        status = state.status
                    )
                )
            } else {
                // UPDATE — uses caseUpdateSchema (all optional)
                caseRepository.updateCase(
                    caseId,
                    UpdateCaseRequest(
                        title = state.title.trim(),
                        caseNumber = state.caseNumber.trim(),
                        courtName = state.courtName.trim(),
                        clientId = state.selectedClientId,
                        judgeName = state.judgeName.trim().ifBlank { null },
                        description = state.description.trim().ifBlank { null },
                        status = state.status
                    )
                )
            }

            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSuccess()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = result.message
                )
                else -> {}
            }
        }
    }

    fun updateTitle(v: String) { _uiState.value = _uiState.value.copy(title = v, error = null) }
    fun updateCaseNumber(v: String) { _uiState.value = _uiState.value.copy(caseNumber = v, error = null) }
    fun updateCourtName(v: String) { _uiState.value = _uiState.value.copy(courtName = v, error = null) }
    fun updateJudgeName(v: String) { _uiState.value = _uiState.value.copy(judgeName = v) }
    fun updateDescription(v: String) { _uiState.value = _uiState.value.copy(description = v) }
    fun updateStatus(v: String) { _uiState.value = _uiState.value.copy(status = v) }
    fun selectClient(id: String, name: String) {
        _uiState.value = _uiState.value.copy(selectedClientId = id, selectedClientName = name, error = null)
    }
}

// ─── SCREEN ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCaseScreen(
    viewModel: AddEditCaseViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var clientMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isLoading) "Loading..." else "Case Entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    if (!uiState.isLoading) {
                        IconButton(
                            onClick = { viewModel.onSave(onSuccess) },
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving)
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            else
                                Icon(Icons.Default.Save, "Save")
                        }
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

                // ── Case Title ──
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("Case Title *") },
                    placeholder = { Text("e.g. State vs. John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error?.contains("title", true) == true
                )

                // ── Case Number ──
                OutlinedTextField(
                    value = uiState.caseNumber,
                    onValueChange = viewModel::updateCaseNumber,
                    label = { Text("Case Number / Reference *") },
                    placeholder = { Text("e.g. CR-2026-001") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error?.contains("number", true) == true
                )

                // ── Court Name ──
                OutlinedTextField(
                    value = uiState.courtName,
                    onValueChange = viewModel::updateCourtName,
                    label = { Text("Court Name *") },
                    placeholder = { Text("e.g. Supreme Court, Dhaka") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error?.contains("court", true) == true
                )

                // ── Judge Name (optional) ──
                OutlinedTextField(
                    value = uiState.judgeName,
                    onValueChange = viewModel::updateJudgeName,
                    label = { Text("Judge Name (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // ── Client Selection Dropdown (required) ──
                ExposedDropdownMenuBox(
                    expanded = clientMenuExpanded,
                    onExpandedChange = { clientMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (uiState.clientsLoading) "Loading clients..."
                                else if (uiState.selectedClientName.isNotBlank()) uiState.selectedClientName
                                else "Select a client *",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Client *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = uiState.error?.contains("client", true) == true,
                        enabled = !uiState.clientsLoading
                    )
                    ExposedDropdownMenu(
                        expanded = clientMenuExpanded,
                        onDismissRequest = { clientMenuExpanded = false }
                    ) {
                        if (uiState.clients.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No clients found. Add a client first.") },
                                onClick = { clientMenuExpanded = false }
                            )
                        } else {
                            uiState.clients.forEach { client ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(client.name, style = MaterialTheme.typography.bodyMedium)
                                            client.phone?.let {
                                                Text(it, style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.secondary)
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectClient(client.id, client.name)
                                        clientMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Description (optional) ──
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Brief Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // ── Status (ACTIVE | CLOSED) — from caseSchema ──
                Text("Status", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ACTIVE", "CLOSED").forEach { status ->
                        FilterChip(
                            selected = uiState.status == status,
                            onClick = { viewModel.updateStatus(status) },
                            label = { Text(status) }
                        )
                    }
                }

                // ── Error Message ──
                uiState.error?.let { errorMsg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // ── Save Button ──
                Button(
                    onClick = { viewModel.onSave(onSuccess) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving && !uiState.clientsLoading
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (uiState.isSaving) "Saving..." else "Save Case")
                }
            }
        }
    }
}
