package com.smarthive.manager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.smarthive.manager.ui.theme.Primary

import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthive.manager.data.HiveViewModel
import com.smarthive.manager.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(navController: NavController, viewModel: HiveViewModel = viewModel()) {
    val profile by viewModel.userProfile.collectAsState(initial = UserProfile())

    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var tempAlerts by remember { mutableStateOf(true) }
    var humidityAlerts by remember { mutableStateOf(true) }
    var harvestReminders by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }

    // Update local state when profile is loaded
    LaunchedEffect(profile) {
        profile?.let {
            pushEnabled = it.pushEnabled
            emailEnabled = it.emailEnabled
            tempAlerts = it.tempAlerts
            humidityAlerts = it.humidityAlerts
            harvestReminders = it.harvestReminders
            marketingEnabled = it.marketingEnabled
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("General Preferences", style = MaterialTheme.typography.labelLarge, color = Primary)
            
            NotificationToggle("Push Notifications", "Receive alerts on your device", pushEnabled) { pushEnabled = it }
            NotificationToggle("Email Notifications", "Receive weekly summaries via email", emailEnabled) { emailEnabled = it }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.2f))
            
            Text("Hive Alerts", style = MaterialTheme.typography.labelLarge, color = Primary)
            
            NotificationToggle("Temperature Alerts", "Critical hive temperature warnings", tempAlerts) { tempAlerts = it }
            NotificationToggle("Humidity Alerts", "Humidity level fluctuations", humidityAlerts) { humidityAlerts = it }
            NotificationToggle("Harvest Reminders", "Notifications when honey flow is high", harvestReminders) { harvestReminders = it }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.2f))
            
            Text("Other", style = MaterialTheme.typography.labelLarge, color = Primary)
            NotificationToggle("Marketing & Tips", "Beekeeping tips and product updates", marketingEnabled) { marketingEnabled = it }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { 
                    val updatedProfile = profile?.copy(
                        pushEnabled = pushEnabled,
                        emailEnabled = emailEnabled,
                        tempAlerts = tempAlerts,
                        humidityAlerts = humidityAlerts,
                        harvestReminders = harvestReminders,
                        marketingEnabled = marketingEnabled
                    ) ?: UserProfile(
                        pushEnabled = pushEnabled,
                        emailEnabled = emailEnabled,
                        tempAlerts = tempAlerts,
                        humidityAlerts = humidityAlerts,
                        harvestReminders = harvestReminders,
                        marketingEnabled = marketingEnabled
                    )
                    
                    viewModel.saveUserProfile(updatedProfile)
                    navController.popBackStack() 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save Preferences", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NotificationToggle(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
        )
    }
}
