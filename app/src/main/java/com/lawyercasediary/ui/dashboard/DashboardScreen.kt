package com.lawyercasediary.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.lawyercasediary.repository.*
import com.lawyercasediary.ui.components.*
import com.lawyercasediary.ui.theme.*
import com.lawyercasediary.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val stats: DashboardStats? = null,
    val profile: UserProfile? = null,
    val todayHearings: List<Hearing> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val imminentHearings: List<UpcomingHearing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(
    private val statsRepository: StatsRepository,
    private val hearingRepository: HearingRepository,
    private val authRepository: AuthRepository,
    private val reminderRepository: ReminderRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { loadDashboardData() }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val statsRes = statsRepository.getDashboardStats()
            val hearingRes = hearingRepository.getHearings()
            val profileRes = authRepository.getProfile()
            val reminderRes = reminderRepository.getReminders()
            val imminentRes = notificationRepository.getUpcomingNotifications()

            val stats = if (statsRes is ApiResult.Success) statsRes.data else null
            val profile = if (profileRes is ApiResult.Success) profileRes.data else null
            val allHearings = if (hearingRes is ApiResult.Success) hearingRes.data else emptyList()
            val reminders = if (reminderRes is ApiResult.Success) reminderRes.data else emptyList()
            val imminent = if (imminentRes is ApiResult.Success) imminentRes.data else emptyList()
            
            val today = LocalDate.now().toString()
            val todayHearings = allHearings.filter { it.hearingDate.startsWith(today) }

            _uiState.value = _uiState.value.copy(
                stats = stats,
                profile = profile,
                todayHearings = todayHearings,
                reminders = reminders.filter { it.status == "PENDING" },
                imminentHearings = imminent,
                isLoading = false,
                error = if (statsRes is ApiResult.Error) statsRes.message else null
            )
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    MainScaffold(
        navController = navController,
        title = "Dashboard",
        profile = uiState.profile
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) LoadingSpinner()
            else if (uiState.error != null) ErrorState(uiState.error!!) { viewModel.loadDashboardData() }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        WelcomeHeader(uiState.profile)
                    }

                    item {
                        PlanBanner(uiState.profile)
                    }

                    item {
                        StatsGrid(uiState.stats)
                    }

                    if (uiState.imminentHearings.isNotEmpty()) {
                        item {
                            SectionTitle("Imminent Hearings", isAlert = true)
                        }
                        items(uiState.imminentHearings) { alert ->
                            ImminentAlertCard(alert)
                        }
                    }
                    
                    if (uiState.reminders.isNotEmpty()) {
                        item {
                            SectionTitle("Recent Reminders")
                        }
                        items(uiState.reminders) { reminder ->
                            ReminderItem(reminder)
                        }
                    }
                    
                    item {
                        SectionTitle("Today's Cause List")
                    }
                    
                    if (uiState.todayHearings.isEmpty()) {
                        item { 
                            EmptyStateCard("No hearings scheduled for today.")
                        }
                    } else {
                        items(uiState.todayHearings) { hearing ->
                            HearingCard(hearing) { /* Open detail */ }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(profile: UserProfile?) {
    Column {
        Text(
            "Hello, Councilor", 
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            profile?.name ?: "Practitioner", 
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        )
    }
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
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Text(
            message, 
            modifier = Modifier.padding(24.dp), 
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ImminentAlertCard(alert: UpcomingHearing) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.PriorityHigh, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    alert.case?.title ?: "Unknown Case", 
                    style = MaterialTheme.typography.titleSmall, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Starts At: ${DateUtils.formatIsoToLocal(alert.hearingDate).takeLast(8)}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun PlanBanner(profile: UserProfile?) {
    val plan = profile?.plan ?: "ESSENTIAL"
    val isUltimate = plan == "ULTIMATE"
    
    val brush = if (isUltimate) {
        Brush.linearGradient(listOf(LegalGold, Color(0xFFB8860B)))
    } else {
        Brush.linearGradient(listOf(LegalBlue40, LegalBlue20))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.background(brush).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isUltimate) Icons.Default.WorkspacePremium else Icons.Default.Shield, 
                            contentDescription = null, 
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "${plan} TIER", 
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black), 
                        color = Color.White
                    )
                    Text(
                        if (isUltimate) "Full Enterprise Access Enabled" 
                        else "Professional Practitioner Access",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderItem(reminder: Reminder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.NotificationsActive, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    reminder.title, 
                    style = MaterialTheme.typography.bodyMedium, 
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Alert set for ${DateUtils.formatIsoToLocal(reminder.remindAt).takeLast(8)}", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun StatsGrid(stats: DashboardStats?) {
    val summary = stats?.stats
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "Active Cases", 
                summary?.activeCases?.toString() ?: "0", 
                Icons.Outlined.Gavel, 
                LegalBlue40,
                Modifier.weight(1f)
            )
            StatCard(
                "Clients", 
                summary?.verifiedClients?.toString() ?: "0", 
                Icons.Outlined.People, 
                Color(0xFF00796B),
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                "Upcoming", 
                summary?.upcomingHearings?.toString() ?: "0", 
                Icons.Outlined.Event, 
                Color(0xFFE65100),
                Modifier.weight(1f)
            )
            StatCard(
                "Uptime", 
                summary?.uptime ?: "Online", 
                Icons.Outlined.Analytics, 
                MaterialTheme.colorScheme.secondary,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = accentColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                value, 
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
