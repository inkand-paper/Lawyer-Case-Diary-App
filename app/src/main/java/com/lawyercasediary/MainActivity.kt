package com.lawyercasediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.lawyercasediary.ui.navigation.AppNavigation
import com.lawyercasediary.ui.navigation.Screen
import com.lawyercasediary.ui.theme.LawyerCaseDiaryTheme
import com.lawyercasediary.notifications.NotificationSyncWorker
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Schedule background sync
        NotificationSyncWorker.schedule(applicationContext)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        val container = (application as LcaApplication).container
        
        setContent {
            LawyerCaseDiaryTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }
                val navController = rememberNavController()

                // Decide start destination based on session
                LaunchedEffect(Unit) {
                    val token = container.sessionManager.accessToken.first()
                    startDestination = if (!token.isNullOrBlank() && token != "null") Screen.Dashboard.route else Screen.Login.route
                }

                // Global session observer: redirect to login if token is cleared
                LaunchedEffect(container.sessionManager.accessToken) {
                    container.sessionManager.accessToken.collect { token ->
                        if ((token.isNullOrBlank() || token == "null") && startDestination != null) {
                            val currentRoute = navController.currentDestination?.route
                            val preAuthRoutes = setOf(
                                Screen.Login.route,
                                Screen.Register.route,
                                Screen.ForgotPassword.route,
                                Screen.VerifyEmail.route
                            )
                            if (currentRoute !in preAuthRoutes) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    startDestination?.let { destination ->
                        AppNavigation(
                            navController = navController,
                            container = container,
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}
