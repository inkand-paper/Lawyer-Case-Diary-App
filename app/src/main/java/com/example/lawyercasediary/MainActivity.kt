package com.example.lawyercasediary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lawyercasediary.ui.theme.LawyerCaseDiaryTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lawyercasediary.di.AppContainer
import com.example.lawyercasediary.ui.auth.LoginScreen
import com.example.lawyercasediary.ui.auth.LoginViewModel
import com.example.lawyercasediary.ui.dashboard.DashboardScreen
import com.example.lawyercasediary.ui.dashboard.DashboardViewModel
import com.example.lawyercasediary.ui.cases.AddCaseScreen
import com.example.lawyercasediary.ui.cases.CaseEditorViewModel

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        
        setContent {
            LawyerCaseDiaryTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        val viewModel = LoginViewModel(container.authRepository)
                        LoginScreen(viewModel) {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                    
                    composable("dashboard") {
                        val viewModel = DashboardViewModel(container.caseRepository)
                        DashboardScreen(
                            viewModel = viewModel,
                            onAddCase = { navController.navigate("add_case") }
                        )
                    }
                    
                    composable("add_case") {
                        val viewModel = CaseEditorViewModel(
                            container.caseRepository,
                            container.clientRepository
                        )
                        AddCaseScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}