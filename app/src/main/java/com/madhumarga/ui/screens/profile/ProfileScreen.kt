package com.madhumarga.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madhumarga.ui.components.ProMembershipDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showProDialog by remember { mutableStateOf(false) }

    if (showProDialog) {
        ProMembershipDialog(onDismiss = { showProDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF3E2723),
                    navigationIconContentColor = Color(0xFF3E2723)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = state.name.ifBlank { "Beekeeper" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = state.title.ifBlank { "Beekeeper" },
                        fontSize = 12.sp,
                        color = Color(0xFFFF8F00),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(value = "${state.yearsExperience}", label = "Years\nExperience", icon = "📅")
                    ProfileStatItem(value = "${state.activeHives}", label = "Active\nHives", icon = "🐝")
                    ProfileStatItem(value = "${state.totalHarvested}", label = "Tons\nHarvested", icon = "🍯")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Managed Apiaries
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Managed Apiaries",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723)
                    )
                    Text("View All", fontSize = 13.sp, color = Color(0xFFFF8F00), fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                val apiaries by viewModel.apiaries.collectAsState(initial = emptyList())
                if (apiaries.isEmpty()) {
                    Text("No apiaries yet", fontSize = 14.sp, color = Color(0xFFBDBDBD))
                } else {
                    apiaries.forEach { apiary ->
                        ApiaryListItem(
                            name = apiary.name,
                            hiveCount = apiary.hiveCount,
                            status = apiary.status
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    "Account Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )

                Spacer(modifier = Modifier.height(8.dp))

                SettingsItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    onClick = onNavigateToEditProfile
                )

                Divider(color = Color(0xFFF0F0F0))

                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notification Preferences",
                    onClick = { viewModel.toggleNotifications() }
                )

                Divider(color = Color(0xFFF0F0F0))

                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Membership Status",
                    subtitle = state.membershipStatus,
                    onClick = { showProDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ProfileStatItem(value: String, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
        Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E), textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}

@Composable
fun ApiaryListItem(name: String, hiveCount: Int, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
        ) {
            Text("🏠", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF3E2723))
            Text("$hiveCount Hives · $status", fontSize = 12.sp, color = Color(0xFF9E9E9E))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF666666), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, color = Color(0xFF3E2723))
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF9E9E9E))
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFBDBDBD))
    }
}
