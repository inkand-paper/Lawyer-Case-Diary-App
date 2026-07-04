package com.lawyercasediary.ui.cases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.CaseRepository
import com.lawyercasediary.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CaseDetailUiState(
    val case: Case? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionError: String? = null,
    val isSharing: Boolean = false
)

class CaseDetailViewModel(
    private val repository: CaseRepository,
    private val caseId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaseDetailUiState())
    val uiState: StateFlow<CaseDetailUiState> = _uiState

    init { loadCaseDetail() }

    fun loadCaseDetail() {
        viewModelScope.launch {
            _uiState.value = CaseDetailUiState(isLoading = true)
            when (val result = repository.getCaseDetail(caseId)) {
                is ApiResult.Success -> _uiState.value = CaseDetailUiState(case = result.data, isLoading = false)
                is ApiResult.Error -> _uiState.value = CaseDetailUiState(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun addNote(content: String) {
        viewModelScope.launch {
            when (val result = repository.addNote(caseId, content)) {
                is ApiResult.Success -> loadCaseDetail()
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(actionError = result.message)
                else -> {}
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteNote(caseId, noteId)) {
                is ApiResult.Success -> loadCaseDetail()
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(actionError = result.message)
                else -> {}
            }
        }
    }

    fun addPayment(amount: Double, method: String?) {
        viewModelScope.launch {
            when (val result = repository.addPayment(caseId, amount, method)) {
                is ApiResult.Success -> loadCaseDetail()
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(actionError = result.message)
                else -> {}
            }
        }
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            when (val result = repository.deletePayment(caseId, paymentId)) {
                is ApiResult.Success -> loadCaseDetail()
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(actionError = result.message)
                else -> {}
            }
        }
    }

    fun shareToChamber() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharing = true, actionError = null)
            when (val result = repository.shareToChamber(caseId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(case = result.data, isSharing = false)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSharing = false, actionError = result.message)
                }
                else -> {}
            }
        }
    }

    fun dismissActionError() {
        _uiState.value = _uiState.value.copy(actionError = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailScreen(
    viewModel: CaseDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNoteDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissActionError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Case Particulars") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    val alreadyShared = uiState.case?.chamberId != null
                    if (!alreadyShared) {
                        IconButton(onClick = { viewModel.shareToChamber() }, enabled = !uiState.isSharing) {
                            if (uiState.isSharing) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            else Icon(Icons.Default.Share, "Share with Chamber")
                        }
                    }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) LoadingSpinner()
            else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadCaseDetail() }
            else uiState.case?.let { case ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(case.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("Ref: ${case.caseNumber}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailRow("Court", case.courtName)
                        DetailRow("Judge", case.judgeName ?: "N/A")
                        DetailRow("Status", case.status)
                        DetailRow("Enrolled On", case.createdAt?.take(10) ?: "N/A")
                        DetailRow("Client", case.client?.name ?: "N/A")
                        DetailRow("Shared with Chamber", if (case.chamberId != null) "Yes" else "No")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Synopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(case.description ?: "No description provided.", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text("Hearings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    val hearings = case.hearings.orEmpty()
                    if (hearings.isEmpty()) {
                        item { Text("No hearings scheduled.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline) }
                    } else {
                        items(hearings, key = { it.id }) { hearing ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(hearing.hearingDate.take(10), fontWeight = FontWeight.SemiBold)
                                    hearing.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showNoteDialog = true }) { Text("Add Note") }
                        }
                    }
                    val notes = case.notes.orEmpty().sortedByDescending { it.createdAt }
                    if (notes.isEmpty()) {
                        item { Text("No notes recorded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline) }
                    } else {
                        items(notes, key = { it.id }) { note ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(note.content, style = MaterialTheme.typography.bodyMedium)
                                        note.createdAt?.let {
                                            Text(it.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                    IconButton(onClick = { viewModel.deleteNote(note.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, "Delete note", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Payments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showPaymentDialog = true }) { Text("Add Payment") }
                        }
                    }
                    val payments = case.payments.orEmpty().sortedByDescending { it.paymentDate }
                    if (payments.isEmpty()) {
                        item { Text("No payments recorded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline) }
                    } else {
                        items(payments, key = { it.id }) { payment ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("$${"%.2f".format(payment.amount)}", fontWeight = FontWeight.Bold)
                                        Text(
                                            "${payment.method ?: "CASH"} · ${payment.paymentDate.take(10)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deletePayment(payment.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, "Delete payment", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(48.dp)) }
                }
            }
        }

        if (showNoteDialog) {
            AddNoteDialog(
                onDismiss = { showNoteDialog = false },
                onAdd = { content ->
                    viewModel.addNote(content)
                    showNoteDialog = false
                }
            )
        }

        if (showPaymentDialog) {
            AddPaymentDialog(
                onDismiss = { showPaymentDialog = false },
                onAdd = { amount, method ->
                    viewModel.addPayment(amount, method)
                    showPaymentDialog = false
                }
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Note") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Note") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (content.isNotBlank()) onAdd(content) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentDialog(onDismiss: () -> Unit, onAdd: (Double, String?) -> Unit) {
    var amountText by remember { mutableStateOf("") }
    val methods = listOf("CASH", "CHECK", "CARD", "BANK_TRANSFER")
    var method by remember { mutableStateOf(methods.first()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Payment") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = method,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Method") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        methods.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { method = option; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = amountText.toDoubleOrNull()
                if (amount != null && amount > 0) onAdd(amount, method)
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
