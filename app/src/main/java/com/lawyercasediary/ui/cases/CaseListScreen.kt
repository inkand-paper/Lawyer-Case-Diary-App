package com.lawyercasediary.ui.cases

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.AuthRepository
import com.lawyercasediary.repository.CaseRepository
import com.lawyercasediary.ui.components.*
import com.lawyercasediary.ui.theme.LegalBlue40
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CaseListUiState(
    val cases: List<Case> = emptyList(),
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val filterStatus: String? = null
)

class CaseListViewModel(
    private val repository: CaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaseListUiState())
    val uiState: StateFlow<CaseListUiState> = _uiState

    init { 
        loadCases() 
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val result = authRepository.getProfile()
            if (result is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(profile = result.data)
            }
        }
    }

    fun loadCases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.getCases(_uiState.value.searchQuery, _uiState.value.filterStatus)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(cases = result.data, isLoading = false, error = null)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                else -> {}
            }
        }
    }

    fun onSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadCases()
    }

    fun onFilter(status: String?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
        loadCases()
    }
}

@Composable
fun CaseListScreen(
    viewModel: CaseListViewModel,
    onCaseClick: (String) -> Unit,
    onAddCase: () -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    MainScaffold(
        navController = navController,
        title = "Case Records",
        profile = uiState.profile,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCase,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Case")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SearchBar(uiState.searchQuery, viewModel::onSearch)
            FilterSection(uiState.filterStatus, viewModel::onFilter)
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) LoadingSpinner()
                else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadCases() }
                else {
                    if (uiState.cases.isEmpty()) {
                        EmptyState("No cases matching your search.", Icons.Default.Gavel)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.cases, key = { it.id }) { case ->
                                CaseCard(case) { onCaseClick(case.id) }
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search case title or number...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun FilterSection(selectedStatus: String?, onStatusChange: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.FilterList, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
        
        val statuses = listOf("ACTIVE", "CLOSED")
        statuses.forEach { status ->
            val isSelected = selectedStatus == status
            FilterChip(
                selected = isSelected,
                onClick = { onStatusChange(if (isSelected) null else status) },
                label = { 
                    Text(
                        status, 
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
