package com.lawyercasediary.ui.clients

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
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
import com.lawyercasediary.repository.ClientRepository
import com.lawyercasediary.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ClientListUiState(
    val clients: List<Client> = emptyList(),
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class ClientListViewModel(
    private val repository: ClientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientListUiState())
    val uiState: StateFlow<ClientListUiState> = _uiState

    init { 
        loadClients() 
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

    fun loadClients() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(
                clients = when (val result = repository.getClients()) {
                    is ApiResult.Success -> result.data
                    else -> emptyList()
                },
                isLoading = false,
                error = null
            )
        }
    }

    fun onSearch(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}

@Composable
fun ClientListScreen(
    viewModel: ClientListViewModel,
    onClientClick: (String) -> Unit,
    onAddClient: () -> Unit,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) visible = true }

    val filteredClients = uiState.clients.filter { 
        it.name.contains(uiState.searchQuery, ignoreCase = true) || 
        (it.phone ?: "").contains(uiState.searchQuery)
    }

    MainScaffold(
        navController = navController,
        title = "Client Directory",
        profile = uiState.profile,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClient,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Client")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SearchBar(uiState.searchQuery, viewModel::onSearch)
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) LoadingSpinner()
                else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadClients() }
                else {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                    ) {
                        if (filteredClients.isEmpty()) {
                            EmptyState("No clients found.", Icons.Default.People)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredClients, key = { it.id }) { client ->
                                    ClientCard(client) { onClientClick(client.id) }
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

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
            placeholder = { Text("Search clients...", style = MaterialTheme.typography.bodyMedium) },
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
fun ClientCard(client: Client, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        client.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    client.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (!client.phone.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Phone, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(4.dp))
                        Text(client.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
                if (!client.email.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Email, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(4.dp))
                        Text(client.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}
