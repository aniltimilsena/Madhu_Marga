package com.madhumarga.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.madhumarga.ui.screens.auth.AuthViewModel
import com.madhumarga.ui.screens.auth.LoginScreen
import com.madhumarga.ui.screens.auth.SignUpScreen
import com.madhumarga.ui.screens.dashboard.DashboardScreen
import com.madhumarga.ui.screens.harvest.HarvestScreen
import com.madhumarga.ui.screens.hive.AddEditHiveScreen
import com.madhumarga.ui.screens.hive.HiveDetailScreen
import com.madhumarga.ui.screens.hive.HiveListScreen
import com.madhumarga.ui.screens.inspection.InspectionScreen
import com.madhumarga.ui.screens.profile.EditProfileScreen
import com.madhumarga.ui.screens.profile.ProfileScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Dashboard : Screen("dashboard")
    data object HiveList : Screen("hive_list")
    data object AddHive : Screen("add_hive")
    data object EditHive : Screen("edit_hive/{hiveId}") {
        fun createRoute(hiveId: Long) = "edit_hive/$hiveId"
    }
    data object HiveDetail : Screen("hive_detail/{hiveId}") {
        fun createRoute(hiveId: Long) = "hive_detail/$hiveId"
    }
    data object Harvest : Screen("harvest/{hiveId}") {
        fun createRoute(hiveId: Long) = "harvest/$hiveId"
    }
    data object Inspection : Screen("inspection/{hiveId}") {
        fun createRoute(hiveId: Long) = "inspection/$hiveId"
    }
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()

    if (!authState.authChecked) return

    val startDestination = if (authState.isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                viewModel = authViewModel
            )
            if (authState.isLoggedIn) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = authViewModel
            )
            if (authState.isLoggedIn) {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                }
            }
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToHives = { navController.navigate(Screen.HiveList.route) },
                onNavigateToAddHive = { navController.navigate(Screen.AddHive.route) },
                onNavigateToHiveDetail = { hiveId ->
                    navController.navigate(Screen.HiveDetail.createRoute(hiveId))
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.HiveList.route) {
            HiveListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddHive = { navController.navigate(Screen.AddHive.route) },
                onNavigateToEditHive = { hiveId ->
                    navController.navigate(Screen.EditHive.createRoute(hiveId))
                },
                onNavigateToHiveDetail = { hiveId ->
                    navController.navigate(Screen.HiveDetail.createRoute(hiveId))
                }
            )
        }

        composable(Screen.AddHive.route) {
            AddEditHiveScreen(
                hiveId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditHive.route,
            arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
        ) { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getLong("hiveId")
            AddEditHiveScreen(
                hiveId = hiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.HiveDetail.route,
            arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
        ) { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: return@composable
            HiveDetailScreen(
                hiveId = hiveId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditHive.createRoute(id))
                },
                onNavigateToInspection = { id ->
                    navController.navigate(Screen.Inspection.createRoute(id))
                },
                onNavigateToHarvest = { id ->
                    navController.navigate(Screen.Harvest.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.Inspection.route,
            arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
        ) { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: return@composable
            InspectionScreen(
                hiveId = hiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Harvest.route,
            arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
        ) { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: return@composable
            HarvestScreen(
                hiveId = hiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
