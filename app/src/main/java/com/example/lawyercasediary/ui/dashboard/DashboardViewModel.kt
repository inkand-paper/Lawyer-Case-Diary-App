package com.example.lawyercasediary.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawyercasediary.models.Case
import com.example.lawyercasediary.models.DashboardStats
import com.example.lawyercasediary.repository.CaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val cases: List<Case>,
        val stats: DashboardStats? = null
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
    object Empty : DashboardUiState()
}

class DashboardViewModel(private val caseRepository: CaseRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            // Parallel execution for performance
            val casesResult = caseRepository.getCases()
            
            casesResult.onSuccess { cases ->
                if (cases.isEmpty()) {
                    _uiState.value = DashboardUiState.Empty
                } else {
                    _uiState.value = DashboardUiState.Success(cases)
                }
            }
            .onFailure { error ->
                _uiState.value = DashboardUiState.Error(error.message ?: "Sync Failed")
            }
        }
    }
}
