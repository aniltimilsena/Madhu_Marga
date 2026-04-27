package com.madhumarga.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.madhumarga.ui.screens.dashboard.DashboardScreen
import com.madhumarga.ui.screens.harvest.HarvestScreen
import com.madhumarga.ui.screens.hive.AddEditHiveScreen
import com.madhumarga.ui.screens.hive.HiveDetailScreen
import com.madhumarga.ui.screens.hive.HiveListScreen
import com.madhumarga.ui.screens.inspection.InspectionScreen
import com.madhumarga.ui.screens.profile.ProfileScreen

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object HiveList : Screen("hive_list")
    data object AddHive : Screen("add_hive")
    data object EditHive : Screen("edit_hive/{hiveId}") {
        fun createRoute(hiveId: Long) = "edit_hive/$hiveId"
    }
    data object HiveDetail : Screen("hive_detail/{hiveId}") {
        fun createRoute(hiveId: Long) = "hive_detail/$hiveId"
    }
    data object Inspection : Screen("inspection/{hiveId}") {
        fun createRoute(hiveId: Long) = "inspection/$hiveId"
    }
    data object Harvest : Screen("harvest/{hiveId}") {
        fun createRoute(hiveId: Long) = "harvest/$hiveId"
    }
    data object Profile : Screen("profile")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToHives = { navController.navigate(Screen.HiveList.route) },
                onNavigateToAddHive = { navController.navigate(Screen.AddHive.route) },
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
            val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: 0L
            AddEditHiveScreen(
                hiveId = hiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.HiveDetail.route,
            arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
        ) { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: 0L
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
            val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: 0L
            InspectionScreen(
                hiveId = hiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Harvest.route,
            arguments = listOf(navArgument("hiveId") { type = NavType.LongType })
        ) { backStackEntry ->
            val hiveId = backStackEntry.arguments?.getLong("hiveId") ?: 0L
            HarvestScreen(
                hiveId = hiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
