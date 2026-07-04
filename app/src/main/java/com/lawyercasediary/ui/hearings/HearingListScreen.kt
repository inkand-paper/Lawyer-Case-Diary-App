package com.lawyercasediary.ui.hearings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.AuthRepository
import com.lawyercasediary.repository.HearingRepository
import com.lawyercasediary.ui.components.*
import com.lawyercasediary.ui.theme.LegalBlue40
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HearingListUiState(
    val hearings: List<Hearing> = emptyList(),
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HearingListViewModel(
    private val repository: HearingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HearingListUiState())
    val uiState: StateFlow<HearingListUiState> = _uiState

    init { 
        loadHearings() 
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

    fun loadHearings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(
                hearings = when (val result = repository.getHearings()) {
                    is ApiResult.Success -> result.data
                    else -> emptyList()
                },
                isLoading = false,
                error = null
            )
        }
    }
}

@Composable
fun HearingListScreen(
    viewModel: HearingListViewModel,
    onAddHearing: (String) -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    MainScaffold(
        navController = navController,
        title = "Cause List",
        profile = uiState.profile,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddHearing("") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Hearing")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) LoadingSpinner()
            else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadHearings() }
            else {
                if (uiState.hearings.isEmpty()) {
                    EmptyState("No hearings scheduled.", Icons.Default.Event)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.hearings) { hearing ->
                            HearingCard(hearing) { /* Open detail */ }
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}
