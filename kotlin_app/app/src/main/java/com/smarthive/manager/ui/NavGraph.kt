package com.smarthive.manager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smarthive.manager.ui.screens.*
import com.smarthive.manager.ui.feedback.FeedbackScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.smarthive.manager.data.AuthViewModel
import com.smarthive.manager.data.AuthState

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    // React to auth state resolution and navigate accordingly
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate("dashboard") {
                popUpTo("loading") { inclusive = true }
                launchSingleTop = true
            }
            is AuthState.Unauthenticated -> navController.navigate("dashboard") {
                // Unauthenticated users can still use the app offline;
                // they'll see the login prompt in profile section.
                popUpTo("loading") { inclusive = true }
                launchSingleTop = true
            }
            else -> { /* Still loading, stay on loading screen */ }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "loading"
    ) {
        composable("loading") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = com.smarthive.manager.ui.theme.Primary)
            }
        }
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("sign_up") { SignUpScreen(navController, authViewModel) }
        composable("dashboard") { HiveDashboardScreen(navController, hiltViewModel()) }
        composable("inspection/{hiveId}") { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getString("hiveId")?.toIntOrNull() ?: 1
            LogInspectionScreen(navController, hiveId, hiltViewModel())
        }
        composable("inspection") {
            LogInspectionScreen(navController, 1, hiltViewModel())
        }
        composable("inspection_history/{hiveId}") { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getString("hiveId")?.toIntOrNull() ?: 1
            InspectionHistoryScreen(navController, hiveId, hiltViewModel())
        }
        composable("alerts") { AlertsHistoryScreen(navController, hiltViewModel()) }
        composable("harvest") { HarvestLogScreen(navController, hiltViewModel()) }
        composable("profile") { BeekeeperProfileScreen(navController, hiltViewModel()) }
        composable("add_hive") { AddHiveScreen(navController, hiltViewModel()) }
        composable("edit_profile") { EditProfileScreen(navController, hiltViewModel()) }
        composable("notifications") { NotificationSettingsScreen(navController, hiltViewModel()) }
        composable("edit_hive/{hiveId}") { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getString("hiveId")?.toIntOrNull() ?: 0
            EditHiveScreen(navController, hiveId, hiltViewModel())
        }
        composable("feedback") { FeedbackScreen(hiltViewModel()) }
    }
}
