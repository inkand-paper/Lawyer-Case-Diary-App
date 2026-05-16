package com.lawyercasediary.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.NotificationRepository
import com.lawyercasediary.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<ApiResult<List<UpcomingHearing>>>(ApiResult.Loading)
    val uiState: StateFlow<ApiResult<List<UpcomingHearing>>> = _uiState

    init { loadNotifications() }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = ApiResult.Loading
            _uiState.value = repository.getUpcomingNotifications()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts & Notices") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is ApiResult.Loading -> LoadingSpinner()
                is ApiResult.Error -> ErrorState(state.message) { viewModel.loadNotifications() }
                is ApiResult.Success -> {
                    if (state.data.isEmpty()) EmptyState("All caught up! No imminent hearings.", Icons.Default.Notifications)
                    else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.data) { hearing ->
                                UpcomingHearingItem(hearing)
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingHearingItem(hearing: UpcomingHearing) {
    ListItem(
        headlineContent = { Text("Hearing within 1 Hour", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
        supportingContent = { 
            Column {
                Text(hearing.case?.title ?: "Unknown Case")
                Text("Case No: ${hearing.case?.caseNumber ?: "N/A"}")
                Text("Court: ${hearing.case?.courtName ?: "N/A"}")
            }
        },
        overlineContent = { Text(hearing.hearingDate.take(16).replace("T", " ")) },
        leadingContent = { 
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    )
}
