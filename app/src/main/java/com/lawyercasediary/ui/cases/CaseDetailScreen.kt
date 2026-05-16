package com.lawyercasediary.ui.cases

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val error: String? = null
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailScreen(
    viewModel: CaseDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Case Particulars") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) LoadingSpinner()
            else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadCaseDetail() }
            else uiState.case?.let { case ->
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(case.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Ref: ${case.caseNumber}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    DetailRow("Court", case.courtName)
                    DetailRow("Judge", case.judgeName ?: "N/A")
                    DetailRow("Status", case.status)
                    DetailRow("Enrolled On", case.createdAt?.take(10) ?: "N/A")
                    DetailRow("Client", case.client?.name ?: "N/A")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Synopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(case.description ?: "No description provided.", style = MaterialTheme.typography.bodyMedium)
                }
            }
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
