package com.lawyercasediary.ui.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.lawyercasediary.models.*
import com.lawyercasediary.repository.AuthRepository
import com.lawyercasediary.ui.components.*
import com.lawyercasediary.ui.navigation.Screen
import com.lawyercasediary.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = authRepository.getProfile()
            if (result is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(profile = result.data, isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = (result as? ApiResult.Error)?.message)
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) visible = true }

    MainScaffold(
        navController = navController,
        title = "Practitioner Profile",
        profile = uiState.profile
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) LoadingSpinner()
            else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadProfile() }
            else if (uiState.profile != null) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            ProfileHeader(uiState.profile!!)
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                        
                        item {
                            ProfileDetailCard(uiState.profile!!)
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        item {
                            LogoutButton {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(profile: UserProfile) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                profile.name.take(1).uppercase(),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            profile.name, 
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            profile.role, 
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileDetailCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            ProfileInfoRow(Icons.Outlined.Email, "Email Address", profile.email)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ProfileInfoRow(Icons.Outlined.VerifiedUser, "Account Status", if (profile.emailVerified) "Verified" else "Pending")
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ProfileInfoRow(Icons.Outlined.CardMembership, "Subscription Plan", profile.plan)
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ProfileInfoRow(Icons.Outlined.Schedule, "Member Since", profile.createdAt?.take(10) ?: "N/A")
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(Icons.Default.Logout, null)
        Spacer(Modifier.width(12.dp))
        Text("Sign Out of Account", fontWeight = FontWeight.Bold)
    }
}
