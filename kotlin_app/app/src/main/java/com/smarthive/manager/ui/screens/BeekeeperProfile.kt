package com.smarthive.manager.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smarthive.manager.ui.theme.Primary
import com.smarthive.manager.ui.theme.Secondary
import com.smarthive.manager.ui.theme.OnSurfaceVariant

import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthive.manager.data.HiveViewModel
import com.smarthive.manager.data.UserProfile
import com.smarthive.manager.data.AuthViewModel
import com.smarthive.manager.data.AuthState
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeekeeperProfileScreen(
    navController: NavController, 
    viewModel: HiveViewModel = viewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val profile by viewModel.userProfile.collectAsState(initial = UserProfile())
    val allHives by viewModel.allHives.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var showProDialog by remember { mutableStateOf(false) }

    if (showProDialog) {
        AlertDialog(
            onDismissRequest = { showProDialog = false },
            title = { Text("Coming Soon") },
            text = { Text("This feature will be available in a future update") },
            confirmButton = {
                TextButton(onClick = { showProDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF064E3B)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Header
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { navController.navigate("edit_profile") }
                ) {
                    if (profile?.imageUri != null) {
                        AsyncImage(
                            model = profile?.imageUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(16.dp), tint = Color.White)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Verified", modifier = Modifier.size(16.dp), tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(profile?.name?.takeIf { it.isNotBlank() } ?: "New Beekeeper", style = MaterialTheme.typography.headlineMedium)
            Surface(color = Color(0xFFFFDCC3), shape = RoundedCornerShape(100.dp), modifier = Modifier.padding(top = 8.dp)) {
                Text(profile?.title?.takeIf { it.isNotBlank() } ?: "Beekeeper", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = Primary)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val totalWeight by viewModel.totalHarvestWeight.collectAsState()
                val hiveCount = allHives.size
                
                StatCard(profile?.experience?.takeIf { it.isNotBlank() } ?: "0", "Years Exp", Icons.Default.CalendarToday, modifier = Modifier.weight(1f))
                StatCard(hiveCount.toString(), "Active Hives", Icons.Default.Hive, modifier = Modifier.weight(1f))
                StatCard("%.1f".format(totalWeight), "kg Harvest", Icons.Default.Sell, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Managed Apiaries Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Managed Apiaries", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = { navController.navigate("dashboard") }) {
                    Text("View All", color = Primary)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Spacer(modifier = Modifier.height(12.dp))
            if (allHives.isNotEmpty()) {
                ApiaryRow(
                    "My Apiaries", 
                    "${allHives.size} Hives • ${allHives.count { it.status == "Healthy" }} Healthy", 
                    Icons.Default.Hive
                ) { navController.navigate("dashboard") }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Text(
                        "No hives added yet. Start by adding a hive from the Dashboard.", 
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Account Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3FF))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Account Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AccountItem(Icons.Default.Edit, "Edit Profile") { 
                        navController.navigate("edit_profile") 
                    }
                    
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    AccountItem(Icons.Default.Notifications, "Notification Preferences") { 
                        navController.navigate("notifications") 
                    }
                    
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    AccountItem(Icons.Default.CardMembership, "Membership Status", subtext = "Pro Plan") {
                        showProDialog = true
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    if (authState is AuthState.Authenticated) {
                        AccountItem(Icons.Default.Logout, "Logout", subtext = "Sign out from Cloud") {
                            authViewModel.logout()
                        }
                    } else {
                        AccountItem(Icons.Default.Login, "Login / Sync", subtext = "Back up data to Cloud") {
                            navController.navigate("login")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun ApiaryRow(name: String, status: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFADEDD3), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Secondary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelLarge)
                Text(status, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun AccountItem(icon: ImageVector, title: String, subtext: String? = null, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    if (subtext != null) {
                        Text(subtext, style = MaterialTheme.typography.labelSmall, color = Color(0xFFB15F00), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.LightGray)
        }
    }
}
