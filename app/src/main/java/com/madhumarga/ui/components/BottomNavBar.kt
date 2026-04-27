package com.madhumarga.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, "dashboard"),
        BottomNavItem("Hives", Icons.Default.Search, "hive_list"),
        BottomNavItem("Profile", Icons.Default.Person, "profile"),
        BottomNavItem("Settings", Icons.Default.Settings, "profile")
    )

    NavigationBar(containerColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF8F00),
                    selectedTextColor = Color(0xFFFF8F00),
                    unselectedIconColor = Color(0xFFBDBDBD),
                    unselectedTextColor = Color(0xFFBDBDBD),
                    indicatorColor = Color(0xFFFFF3E0)
                )
            )
        }
    }
}
