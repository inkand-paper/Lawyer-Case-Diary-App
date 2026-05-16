package com.lawyercasediary.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import com.lawyercasediary.models.UserProfile
import com.lawyercasediary.ui.navigation.Screen
import com.lawyercasediary.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController,
    title: String,
    profile: UserProfile?,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                profile?.let {
                    DrawerHeader(it)
                } ?: Box(modifier = Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                
                HorizontalDivider(
                    Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                DrawerItem(
                    label = "Practitioner Profile",
                    icon = Icons.Outlined.AccountCircle,
                    onClick = { 
                        navController.navigate(Screen.Profile.route)
                        scope.launch { drawerState.close() }
                    }
                )
                
                DrawerItem(
                    label = "Settings",
                    icon = Icons.Outlined.Settings,
                    onClick = { /* Settings */ }
                )
                
                DrawerItem(
                    label = "Notifications",
                    icon = Icons.Outlined.Notifications,
                    onClick = { 
                        navController.navigate(Screen.Notifications.route)
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(Modifier.weight(1f))
                
                DrawerItem(
                    label = "Logout",
                    icon = Icons.Outlined.Logout,
                    onClick = { 
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            title, 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Notes, "Menu") // Modern menu icon
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Screen.Notifications.route) },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(Icons.Outlined.Notifications, "Alerts")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            },
            bottomBar = { GlobalBottomBar(navController) },
            floatingActionButton = floatingActionButton,
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                content(PaddingValues(0.dp))
            }
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, null, tint = color) },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DrawerHeader(profile: UserProfile) {
    Column(modifier = Modifier.padding(24.dp)) {
        Box(
            modifier = Modifier
                .size(72.dp)
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
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            profile.name, 
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            profile.email, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            color = if (profile.plan == "ULTIMATE") LegalGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                profile.plan, 
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                color = if (profile.plan == "ULTIMATE") Color(0xFF8B7500) else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun GlobalBottomBar(navController: NavController) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            val items = listOf(
                ScreenItem(Screen.Dashboard.route, Icons.Default.GridView, Icons.Outlined.GridView, "Home"),
                ScreenItem(Screen.CaseList.route, Icons.Default.Gavel, Icons.Outlined.Gavel, "Cases"),
                ScreenItem(Screen.HearingList.route, Icons.Default.CalendarToday, Icons.Outlined.CalendarToday, "Cause List"),
                ScreenItem(Screen.Chamber.route, Icons.Default.Domain, Icons.Outlined.Domain, "Chamber"),
                ScreenItem(Screen.ClientList.route, Icons.Default.PeopleAlt, Icons.Outlined.PeopleAlt, "Clients")
            )

            items.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (isSelected) item.selectedIcon else item.unselectedIcon, 
                            contentDescription = item.label
                        ) 
                    },
                    label = { 
                        Text(
                            item.label, 
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        ) 
                    },
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

data class ScreenItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)
