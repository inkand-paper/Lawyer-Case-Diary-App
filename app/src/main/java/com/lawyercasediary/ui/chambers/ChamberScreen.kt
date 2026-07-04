package com.lawyercasediary.ui.chambers

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.AuthRepository
import com.lawyercasediary.repository.ChamberRepository
import com.lawyercasediary.ui.components.*
import com.lawyercasediary.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChamberUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chamber: Chamber? = null,
    val invites: List<Invitation> = emptyList(),
    val profile: UserProfile? = null,
    val messages: List<ChamberMessage> = emptyList(),
    val isSendingMessage: Boolean = false,
    val chatError: String? = null
)

class ChamberViewModel(
    private val repository: ChamberRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChamberUiState(isLoading = true))
    val uiState: StateFlow<ChamberUiState> = _uiState
    private var pollingJob: kotlinx.coroutines.Job? = null

    init { 
        loadChamberData() 
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

    fun loadChamberData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val chamberResult = repository.getChamber()
            val invitesResult = repository.getInvites()
            
            val chamber = if (chamberResult is ApiResult.Success) chamberResult.data else null
            val invites = if (invitesResult is ApiResult.Success) invitesResult.data else emptyList()
            
            val error = if (chamberResult is ApiResult.Error) chamberResult.message else null
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                chamber = chamber,
                invites = invites,
                error = error
            )

            // Only start polling chat once we know the user is actually in a
            // chamber — the messages endpoint 403s otherwise on every call.
            if (chamber != null) startMessagePolling()
        }
    }

    private fun startMessagePolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                val result = repository.getMessages()
                if (result is ApiResult.Success) {
                    _uiState.value = _uiState.value.copy(messages = result.data)
                }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingMessage = true, chatError = null)
            when (val result = repository.sendMessage(content)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSendingMessage = false,
                        messages = _uiState.value.messages + result.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSendingMessage = false, chatError = result.message)
                }
                else -> {}
            }
        }
    }

    fun createChamber(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.createChamber(CreateChamberRequest(name))) {
                is ApiResult.Success -> {
                    loadChamberData()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }
    
    fun sendInvite(email: String, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = repository.sendInvite(CreateInvitationRequest(email, role))) {
                is ApiResult.Success -> {
                    loadChamberData()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ChamberScreen(
    viewModel: ChamberViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) visible = true }

    MainScaffold(
        navController = navController,
        title = "Chamber",
        profile = uiState.profile,
        floatingActionButton = {
            if (uiState.chamber == null && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Chamber")
                }
            } else if (uiState.chamber != null && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = { showInviteDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Outlined.Mail, contentDescription = "Invite Member")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) LoadingSpinner()
            else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadChamberData() }
            else {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                ) {
                    if (uiState.chamber == null) {
                        EmptyState("Not a member of any chamber.", Icons.Outlined.Domain)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                ChamberHeroCard(uiState.chamber!!)
                            }
                            
                            item {
                                SectionTitle("Sent Invitations")
                            }
                            
                            if (uiState.invites.isEmpty()) {
                                item {
                                    Text(
                                        "No pending invitations.", 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            } else {
                                items(uiState.invites) { invite ->
                                    InviteCard(invite)
                                }
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SectionTitle("Chamber Chat")
                            }
                            item {
                                ChamberChatSection(
                                    messages = uiState.messages,
                                    currentUserId = uiState.profile?.id,
                                    isSending = uiState.isSendingMessage,
                                    chatError = uiState.chatError,
                                    onSend = { viewModel.sendMessage(it) }
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateChamberDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name -> 
                    viewModel.createChamber(name)
                    showCreateDialog = false
                }
            )
        }
        
        if (showInviteDialog) {
            InviteMemberDialog(
                onDismiss = { showInviteDialog = false },
                onInvite = { email, role -> 
                    viewModel.sendInvite(email, role)
                    showInviteDialog = false
                }
            )
        }
    }
}

@Composable
fun ChamberHeroCard(chamber: Chamber) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Outlined.Domain, 
                        null, 
                        modifier = Modifier.padding(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        chamber.name, 
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        "Legal Chamber", 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoBadge(Icons.Outlined.Group, "${chamber.members.size} Associates")
                InfoBadge(Icons.Outlined.Person, "Owner: Councilor")
            }
        }
    }
}

@Composable
fun InfoBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun InviteCard(invite: Invitation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Icon(
                    Icons.Outlined.Mail, 
                    null, 
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(invite.email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("Role: ${invite.role}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            
            Surface(
                color = if (invite.status == "PENDING") Color(0xFFFFA500).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    invite.status, 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (invite.status == "PENDING") Color(0xFFCC8400) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun CreateChamberDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Establish Chamber", fontWeight = FontWeight.Black) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Chamber Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun InviteMemberDialog(onDismiss: () -> Unit, onInvite: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("MEMBER") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite Associate", fontWeight = FontWeight.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Assigned Role", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = role == "MEMBER", onClick = { role = "MEMBER" })
                    Text("Member", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(24.dp))
                    RadioButton(selected = role == "ADMIN", onClick = { role = "ADMIN" })
                    Text("Admin", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (email.isNotBlank()) onInvite(email, role) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Send Invitation") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
@Composable
fun SectionTitle(title: String, isAlert: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isAlert) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            title, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.ExtraBold,
            color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ChamberChatSection(
    messages: List<com.lawyercasediary.models.ChamberMessage>,
    currentUserId: String?,
    isSending: Boolean,
    chatError: String?,
    onSend: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (messages.isEmpty()) {
                Text(
                    "No messages yet. Start the conversation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val isMine = message.userId == currentUserId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isMine) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(10.dp),
                                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                            ) {
                                if (!isMine) {
                                    Text(
                                        message.user?.name ?: "Unknown",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(message.content, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            chatError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { if (it.length <= 2000) draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message your chamber...") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            onSend(draft)
                            draft = ""
                        }
                    },
                    enabled = !isSending && draft.isNotBlank()
                ) {
                    if (isSending) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}
