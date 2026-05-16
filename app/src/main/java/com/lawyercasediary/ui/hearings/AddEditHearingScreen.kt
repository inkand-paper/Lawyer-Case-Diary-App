package com.lawyercasediary.ui.hearings

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.CaseRepository
import com.lawyercasediary.repository.HearingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditHearingViewModel(
    private val repository: HearingRepository,
    private val caseRepository: CaseRepository,
    private val caseId: String?
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddEditHearingUiState(selectedCaseId = caseId ?: ""))
    val uiState: StateFlow<AddEditHearingUiState> = _uiState

    init {
        loadCases()
    }

    private fun loadCases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(casesLoading = true)
            when (val result = caseRepository.getCases()) {
                is ApiResult.Success -> {
                    val cases = result.data
                    val selectedName = cases.find { it.id == caseId }?.title ?: ""
                    _uiState.value = _uiState.value.copy(
                        cases = cases,
                        casesLoading = false,
                        selectedCaseName = selectedName
                    )
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(casesLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun onSave(onSuccess: () -> Unit) {
        val selectedId = _uiState.value.selectedCaseId
        if (selectedId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please select a case.")
            return
        }
        if (_uiState.value.date.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Hearing date is required.")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val request = CreateHearingRequest(
                caseId = selectedId,
                hearingDate = _uiState.value.date,
                nextDate = _uiState.value.nextDate.takeIf { it.isNotBlank() },
                notes = _uiState.value.notes
            )
            
            when (val result = repository.createHearing(request)) {
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

    fun updateDate(v: String) { _uiState.value = _uiState.value.copy(date = v) }
    fun updateNextDate(v: String) { _uiState.value = _uiState.value.copy(nextDate = v) }
    fun updateNotes(v: String) { _uiState.value = _uiState.value.copy(notes = v) }
    fun selectCase(id: String, name: String) {
        _uiState.value = _uiState.value.copy(selectedCaseId = id, selectedCaseName = name)
    }
}

data class AddEditHearingUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
    val date: String = "",
    val nextDate: String = "",
    val notes: String = "",
    val selectedCaseId: String = "",
    val selectedCaseName: String = "",
    val cases: List<Case> = emptyList(),
    val casesLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHearingScreen(
    viewModel: AddEditHearingViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var caseMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Hearing") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("Case Association", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = caseMenuExpanded,
                onExpandedChange = { caseMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = if (uiState.casesLoading) "Loading cases..."
                            else if (uiState.selectedCaseName.isNotBlank()) uiState.selectedCaseName
                            else "Select a Case *",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Case *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = caseMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = !uiState.casesLoading
                )
                ExposedDropdownMenu(
                    expanded = caseMenuExpanded,
                    onDismissRequest = { caseMenuExpanded = false }
                ) {
                    uiState.cases.forEach { case ->
                        DropdownMenuItem(
                            text = { Text(case.title) },
                            onClick = {
                                viewModel.selectCase(case.id, case.title)
                                caseMenuExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Judicial Event Details", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            DatePickerField(
                label = "Hearing Date",
                value = uiState.date,
                onValueChange = viewModel::updateDate,
                context = context
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Future Planning (Triggers Reminders)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            
            DatePickerField(
                label = "Next Date (Optional)",
                value = uiState.nextDate,
                onValueChange = viewModel::updateNextDate,
                context = context
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Procedural Notes / Summary") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Enter evidence status, witness details, etc.") }
            )
            
            uiState.error?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(label: String, value: String, onValueChange: (String) -> Unit, context: Context) {
    val calendar = Calendar.getInstance()
    
    OutlinedTextField(
        value = if (value.isNotEmpty()) value.take(16).replace("T", " ") else "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = {
                DatePickerDialog(context, { _, year, month, day ->
                    TimePickerDialog(context, { _, hour, minute ->
                        val selected = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute)
                        }
                        onValueChange(com.lawyercasediary.utils.DateUtils.toIsoString(selected))
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }) {
                Icon(Icons.Default.Event, contentDescription = "Pick Date")
            }
        }
    )
}
