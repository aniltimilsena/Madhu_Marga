package com.smarthive.manager.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarthive.manager.ui.theme.Primary
import com.smarthive.manager.ui.theme.Secondary
import com.smarthive.manager.ui.theme.OnSurfaceVariant
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthive.manager.data.Hive
import com.smarthive.manager.data.HiveViewModel
import com.smarthive.manager.data.UserProfile
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlertsHistoryScreen(
    navController: androidx.navigation.NavController,
    viewModel: HiveViewModel = viewModel()
) {
    val inspections by viewModel.allInspections.collectAsState(initial = emptyList())
    val profile by viewModel.userProfile.collectAsState(initial = UserProfile())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var expandedItems by remember { mutableStateOf(setOf<String>()) }
    var filterQuery by remember { mutableStateOf("") }

    val filteredHistory = remember(filterQuery, inspections) {
        inspections.filter { 
            it.notes.contains(filterQuery, ignoreCase = true) || 
            it.healthIssues.contains(filterQuery, ignoreCase = true) 
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.navigate("profile") }
                    ) {
                        val currentProfile = profile
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentProfile?.imageUri != null) {
                                AsyncImage(
                                    model = currentProfile.imageUri,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            currentProfile?.name?.takeIf { it.isNotBlank() } ?: "Hive Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF064E3B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                val allHives by viewModel.allHives.collectAsState(initial = emptyList())
                val alertCount = allHives.count { it.status == "Alert" || it.status == "Warning" }
                val isCritical = allHives.any { it.status == "Alert" }
                
                Spacer(modifier = Modifier.height(20.dp))
                AlertsHeader(alertCount, isCritical)
                Spacer(modifier = Modifier.height(16.dp))
                ActiveAlertsGrid(hives = allHives, navController = navController)
            }

            item {
                HistoryHeader(filterQuery, onFilterChange = { filterQuery = it })
            }

            items(filteredHistory) { item ->
                val isExpanded = expandedItems.contains(item.id.toString())
                var showDeleteConfirm by remember { mutableStateOf(false) }

                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        title = { Text("Delete Log?") },
                        text = { Text("Are you sure you want to permanently delete this inspection record from ${item.date}?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteInspection(item)
                                    showDeleteConfirm = false
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                        }
                    )
                }

                HistoryItemCard(
                    navController = navController,
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    item = HistoryItem(
                        title = if (item.healthIssues.contains("Healthy")) "Routine Check" else "Health Issue: ${item.healthIssues}",
                        date = item.date,
                        hives = "Hive #${item.hiveId}",
                        status = if (item.healthIssues.contains("Healthy")) "Healthy" else "Warning",
                        icon = if (item.healthIssues.contains("Healthy")) Icons.Default.AssignmentTurnedIn else Icons.Default.Warning,
                        iconBg = if (item.healthIssues.contains("Healthy")) Color(0xFFE8F5E9) else Color(0xFFFFDAD6),
                        iconColor = if (item.healthIssues.contains("Healthy")) Color(0xFF2E7D32) else Color(0xFF93000A),
                        isExpanded = isExpanded,
                        notes = item.notes,
                        queenSpotted = if (item.queenPresence) "Yes" else "No",
                        temperament = if (item.temperament < 0.3f) "Aggressive" else if (item.temperament < 0.7f) "Gentle" else "Very Calm",
                        isSynced = item.isSynced
                    ),
                    onClick = {
                        val idStr = item.id.toString()
                        expandedItems = if (isExpanded) {
                            expandedItems - idStr
                        } else {
                            expandedItems + idStr
                        }
                    },
                    onDelete = { showDeleteConfirm = true }
                )
            }
        }
    }
}

@Composable
fun AlertsHeader(count: Int, isCritical: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Primary)
            Text("Active Alerts", style = MaterialTheme.typography.headlineMedium)
        }
        
        if (count > 0) {
            val countText = if (isCritical) "$count CRITICAL" else "$count ACTIVE"
            val bgColor = if (isCritical) Color(0xFFFFDAD6) else Color(0xFFFEF3C7)
            val textColor = if (isCritical) Color(0xFF93000A) else Color(0xFF464021)

            Surface(color = bgColor, shape = RoundedCornerShape(100.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), 
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(textColor, CircleShape))
                    Text(countText, style = MaterialTheme.typography.labelSmall, color = textColor)
                }
            }
        } else {
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(100.dp)) {
                Text(
                    "NORMAL", 
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun ActiveAlertsGrid(hives: List<Hive>, navController: androidx.navigation.NavController) {
    val alertHives = hives.filter { it.status == "Alert" || it.status == "Warning" }

    if (alertHives.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                Text("All systems normal. No active alerts for your colonies.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            alertHives.forEach { hive ->
                AlertSmallCard(
                    title = if (hive.status == "Alert") "CRITICAL: ${hive.name}" else "WARNING: ${hive.name}",
                    desc = if (hive.status == "Alert") "High temperature (${hive.temp}) detected. Immediate action required!" else "Temperature deviation (${hive.temp}) noted.",
                    time = hive.lastInspected,
                    icon = if (hive.status == "Alert") Icons.Default.Error else Icons.Default.Warning,
                    containerColor = if (hive.status == "Alert") Color(0xFFFFDAD6) else Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth(),
                    // Navigate to Edit screen so user can update temperature or review the hive
                    onClick = { navController.navigate("edit_hive/${hive.id}") }
                )
            }
        }
    }
}

@Composable
fun AlertSmallCard(
    title: String,
    desc: String,
    time: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp))
            Text(desc, style = MaterialTheme.typography.labelSmall, color = Color.DarkGray.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
            Text(time.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryHeader(query: String, onFilterChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.History, contentDescription = null, tint = Primary)
                Text("Inspection History", style = MaterialTheme.typography.headlineMedium)
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        OutlinedTextField(
            value = query,
            onValueChange = onFilterChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Filter history...", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onFilterChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun HistoryItemCard(
    navController: androidx.navigation.NavController, 
    viewModel: HiveViewModel, 
    snackbarHostState: SnackbarHostState,
    item: HistoryItem, 
    onClick: () -> Unit, 
    onDelete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (item.isExpanded) Primary.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().background(if (item.isExpanded) Color(0xFFF9F9F9) else Color.White).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.size(40.dp).background(item.iconBg, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                        Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(item.title, style = MaterialTheme.typography.headlineSmall)
                        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconText(Icons.Default.CalendarToday, item.date)
                            IconText(Icons.Default.Hive, item.hives)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusIndicator(status = item.status, showText = item.status != "Healthy")
                    if (!item.isSynced) {
                        Icon(Icons.Default.CloudOff, contentDescription = "Not synced", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.CloudDone, contentDescription = "Synced", tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    Icon(if (item.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color.LightGray)
                }
            }
            
            if (item.isExpanded) {
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Detailed Logs Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Detailed Logs", style = MaterialTheme.typography.labelLarge, color = Color(0xFF064E3B))
                            Spacer(Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                val itemModifier = Modifier.weight(1f)
                                LogDetailGridItem("Queen", item.queenSpotted, Icons.Default.BrightnessLow, itemModifier)
                                LogDetailGridItem("Temp.", item.temperament, Icons.Default.Speed, itemModifier)
                            }
                            
                            if (item.notes.isNotBlank()) {
                                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    Text("Notes:", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Text(item.notes, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    // AI Recommendations Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3FF)),
                        border = BorderStroke(1.dp, Color(0xFFDCE2F7))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("AI Recommendations", style = MaterialTheme.typography.labelLarge, color = Primary)
                            Spacer(Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RecommendationChip(
                                    text = "Monitor health", 
                                    icon = Icons.Default.Visibility, 
                                    modifier = Modifier.weight(1f),
                                    onClick = { navController.navigate("dashboard") }
                                )
                                RecommendationChip(
                                    text = "Schedule follow-up", 
                                    icon = Icons.Default.Event, 
                                    modifier = Modifier.weight(1f),
                                    onClick = { 
                                        val calendar = java.util.Calendar.getInstance()
                                        android.app.DatePickerDialog(
                                            context,
                                            { _, year, month, day ->
                                                val date = "$year-${month + 1}-$day"
                                                val reminder = com.smarthive.manager.data.Reminder(
                                                    hiveId = item.hives.replace("Hive #", "").toIntOrNull() ?: 1,
                                                    date = date,
                                                    description = "Follow-up for inspection on ${item.date}"
                                                )
                                                viewModel.insertReminder(reminder)
                                                viewModel.scheduleReminder(reminder)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("📅 Follow-up scheduled for $date")
                                                }
                                            },
                                            calendar.get(java.util.Calendar.YEAR),
                                            calendar.get(java.util.Calendar.MONTH),
                                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (item.status == "Healthy") "Colony is thriving. No immediate action needed." 
                                else "Monitor closely and consider treatment if issues persist.", 
                                style = MaterialTheme.typography.labelSmall,
                                color = Secondary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(
                                onClick = onDelete,
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Delete Record")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconText(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnSurfaceVariant)
        Text(text, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
fun LogDetailGridItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.size(28.dp).background(Color(0xFFF1F3FF), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Secondary)
        }
    }
}

@Composable
fun RecommendationChip(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFDCE2F7))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

data class HistoryItem(
    val title: String,
    val date: String,
    val hives: String,
    val status: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconColor: Color,
    val isExpanded: Boolean = false,
    val notes: String = "",
    val queenSpotted: String = "",
    val temperament: String = "",
    val isSynced: Boolean = true
)
