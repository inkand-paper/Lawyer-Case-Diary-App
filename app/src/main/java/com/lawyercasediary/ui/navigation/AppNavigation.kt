package com.lawyercasediary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.lawyercasediary.ui.auth.*
import com.lawyercasediary.ui.dashboard.*
import com.lawyercasediary.ui.cases.*
import com.lawyercasediary.ui.hearings.*
import com.lawyercasediary.ui.clients.*
import com.lawyercasediary.ui.chambers.*
import com.lawyercasediary.ui.notifications.*
import com.lawyercasediary.ui.profile.*
import com.lawyercasediary.di.AppContainer

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object VerifyEmail : Screen("verify_email/{email}") {
        fun createRoute(email: String) = "verify_email/${java.net.URLEncoder.encode(email, "UTF-8")}"
    }
    object Dashboard : Screen("dashboard")
    object CaseList : Screen("cases")
    object CaseDetail : Screen("case_detail/{id}") {
        fun createRoute(id: String) = "case_detail/$id"
    }
    object AddEditCase : Screen("add_edit_case?id={id}") {
        fun createRoute(id: String? = null) = if (id != null) "add_edit_case?id=$id" else "add_edit_case?id="
    }
    object HearingList : Screen("hearings")
    object AddEditHearing : Screen("add_edit_hearing?caseId={caseId}") {
        fun createRoute(caseId: String? = null) = if (caseId != null) "add_edit_hearing?caseId=$caseId" else "add_edit_hearing?caseId="
    }
    object ClientList : Screen("clients")
    object AddEditClient : Screen("add_edit_client?id={id}") {
        fun createRoute(id: String? = null) = if (id != null) "add_edit_client?id=$id" else "add_edit_client?id="
    }
    object ClientDetail : Screen("client_detail/{id}") {
        fun createRoute(id: String) = "client_detail/$id"
    }
    object Chamber : Screen("chamber")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    container: AppContainer,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = LoginViewModel(container.authRepository),
                onLoginSuccess = { 
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = RegisterViewModel(container.authRepository),
                onRegisterSuccess = { email ->
                    navController.navigate(Screen.VerifyEmail.createRoute(email)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = ForgotPasswordViewModel(container.authRepository),
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VerifyEmail.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedEmail = backStackEntry.arguments?.getString("email") ?: ""
            val email = java.net.URLDecoder.decode(encodedEmail, "UTF-8")
            VerifyEmailScreen(
                email = email,
                viewModel = VerifyViewModel(container.authRepository),
                onVerified = {
                    // Verifying doesn't create a full (refreshable) session — see
                    // register/route.ts, that's issued on login only — so send
                    // the user to Login to actually establish one.
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.VerifyEmail.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = DashboardViewModel(
                    container.statsRepository, 
                    container.hearingRepository,
                    container.authRepository,
                    container.reminderRepository,
                    container.notificationRepository
                ),
                navController = navController
            )
        }

        composable(Screen.CaseList.route) {
            CaseListScreen(
                viewModel = CaseListViewModel(container.caseRepository, container.authRepository),
                onCaseClick = { id -> navController.navigate(Screen.CaseDetail.createRoute(id)) },
                onAddCase = { navController.navigate(Screen.AddEditCase.createRoute()) },
                navController = navController
            )
        }

        composable(Screen.CaseDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            CaseDetailScreen(
                viewModel = CaseDetailViewModel(container.caseRepository, id),
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.AddEditCase.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddEditCase.route,
            arguments = listOf(
                navArgument("id") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.takeIf { it.isNotBlank() }
            AddEditCaseScreen(
                viewModel = AddEditCaseViewModel(container.caseRepository, container.clientRepository, id),
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.HearingList.route) {
            HearingListScreen(
                viewModel = HearingListViewModel(container.hearingRepository, container.authRepository),
                onAddHearing = { caseId -> navController.navigate(Screen.AddEditHearing.createRoute(caseId)) },
                navController = navController
            )
        }

        composable(
            route = Screen.AddEditHearing.route,
            arguments = listOf(
                navArgument("caseId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId")?.takeIf { it.isNotBlank() }
            AddEditHearingScreen(
                viewModel = AddEditHearingViewModel(container.hearingRepository, container.caseRepository, caseId),
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ClientList.route) {
            ClientListScreen(
                viewModel = ClientListViewModel(container.clientRepository, container.authRepository),
                onClientClick = { id -> navController.navigate(Screen.ClientDetail.createRoute(id)) },
                onAddClient = { navController.navigate(Screen.AddEditClient.createRoute()) },
                navController = navController
            )
        }

        composable(
            route = Screen.AddEditClient.route,
            arguments = listOf(
                navArgument("id") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.takeIf { it.isNotBlank() }
            AddEditClientScreen(
                viewModel = AddEditClientViewModel(container.clientRepository, id),
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ClientDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            ClientDetailScreen(
                viewModel = ClientDetailViewModel(container.clientRepository, id),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chamber.route) {
            ChamberScreen(
                viewModel = ChamberViewModel(container.chamberRepository, container.authRepository),
                navController = navController
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                viewModel = NotificationViewModel(container.notificationRepository),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = ProfileViewModel(container.authRepository),
                navController = navController
            )
        }
    }
}
