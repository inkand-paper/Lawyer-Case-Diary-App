package com.example.lawyercasediary.ui.cases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawyercasediary.models.Client
import com.example.lawyercasediary.repository.CaseRepository
import com.example.lawyercasediary.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CaseEditorState {
    object Idle : CaseEditorState()
    object Loading : CaseEditorState()
    object Success : CaseEditorState()
    data class Error(val message: String) : CaseEditorState()
}

class CaseEditorViewModel(
    private val caseRepository: CaseRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<CaseEditorState>(CaseEditorState.Idle)
    val state: StateFlow<CaseEditorState> = _state

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients

    init {
        loadClients()
    }

    private fun loadClients() {
        viewModelScope.launch {
            clientRepository.getClients().onSuccess { _clients.value = it }
        }
    }

    fun createCase(title: String, caseNumber: String, courtName: String, clientId: String) {
        viewModelScope.launch {
            _state.value = CaseEditorState.Loading
            caseRepository.createCase(title, caseNumber, courtName, clientId)
                .onSuccess { _state.value = CaseEditorState.Success }
                .onFailure { _state.value = CaseEditorState.Error(it.message ?: "Save Failed") }
        }
    }
}
