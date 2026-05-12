package com.smarthive.manager.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarthive.manager.ui.theme.Primary
import com.smarthive.manager.ui.theme.Secondary
import com.smarthive.manager.ui.theme.SecondaryContainer
import com.smarthive.manager.ui.theme.OnSurfaceVariant
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthive.manager.data.Hive
import com.smarthive.manager.data.HiveViewModel
import com.smarthive.manager.data.UserProfile
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDashboardScreen(
    navController: androidx.navigation.NavController,
    viewModel: HiveViewModel = viewModel()
) {
    val allHives by viewModel.allHives.collectAsState(initial = emptyList())
    val profile by viewModel.userProfile.collectAsState(initial = UserProfile())
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var sortByName by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullToRefreshState()

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(pullRefreshState.isRefreshing) {
            viewModel.refreshAll()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullRefreshState.startRefresh()
        } else {
            pullRefreshState.endRefresh()
        }
    }

    val filteredHives = remember(allHives, statusFilter, sortByName) {
        var list = if (statusFilter == null) allHives else allHives.filter { it.status == statusFilter }
        if (sortByName) list.sortedBy { it.name } else list
    }

    Scaffold(
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUri = currentProfile?.imageUri
                            if (photoUri != null) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Profile Picture of ${currentProfile.name}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = "Default Profile Icon", tint = Primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            currentProfile?.name?.takeIf { it.isNotBlank() } ?: "Hive Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF064E3B),
                            modifier = Modifier.semantics { contentDescription = "User: ${currentProfile?.name ?: "Hive Manager"}" }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Open Notifications Settings")
                    }
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Open User Profile Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_hive") },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Hive")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 80.dp)
        ) {
            item {
                val healthy by viewModel.healthyCount.collectAsState()
                val warning by viewModel.warningCount.collectAsState()
                val alert by viewModel.alertCount.collectAsState()
                ActiveFleetSummary(allHives.size, healthy, warning, alert)
                Spacer(modifier = Modifier.height(24.dp))
                
                // Apiary Activity & General Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hive, contentDescription = null, tint = Primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${allHives.size} Active Colonies", style = MaterialTheme.typography.titleLarge, color = Primary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Regular monitoring is essential for maintaining colony stability and maximizing seasonal honey production. Track your vitals daily to identify trends and ensure hive safety.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HealthAnalysisCard(healthy, warning, alert)
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Apiary Overview", style = MaterialTheme.typography.headlineMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterButton(Icons.Default.FilterList, isActive = statusFilter != null) {
                            statusFilter = when(statusFilter) {
                                null -> "Healthy"
                                "Healthy" -> "Warning"
                                "Warning" -> "Alert"
                                else -> null
                            }
                        }
                        FilterButton(Icons.AutoMirrored.Filled.Sort, isActive = sortByName) { 
                            sortByName = !sortByName
                        }
                    }
                }
            }
            
            if (filteredHives.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No hives added yet. Click + to add your first colony!", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            } else {
                items(filteredHives) { hive ->
                    HiveCard(hive, navController, onClick = { navController.navigate("inspection/${hive.id}") })
                }
            }
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun HealthAnalysisCard(healthy: Int, warning: Int, alert: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "HEALTH ANALYSIS", 
                style = MaterialTheme.typography.labelMedium, 
                color = Primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            HealthDistributionChart(
                healthy = healthy.toFloat(),
                warning = warning.toFloat(),
                alert = alert.toFloat(),
                modifier = Modifier.size(160.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartLegendItem("Healthy", Secondary)
                ChartLegendItem("Warning", Color(0xFFD97706))
                ChartLegendItem("Alert", MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
fun HealthDistributionChart(healthy: Float, warning: Float, alert: Float, modifier: Modifier = Modifier) {
    val total = healthy + warning + alert
    if (total == 0f) {
        Box(modifier = modifier.clip(CircleShape).background(Color.LightGray.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Text("No Data", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        return
    }

    val healthyAngle = (healthy / total) * 360f
    val warningAngle = (warning / total) * 360f
    val alertAngle = (alert / total) * 360f

    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.15f
        
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.1f),
            style = Stroke(width = strokeWidth)
        )

        drawArc(
            color = Secondary,
            startAngle = -90f,
            sweepAngle = healthyAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            color = Color(0xFFD97706),
            startAngle = -90f + healthyAngle,
            sweepAngle = warningAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        drawArc(
            color = Color(0xFFEF4444),
            startAngle = -90f + healthyAngle + warningAngle,
            sweepAngle = alertAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ActiveFleetSummary(count: Int, healthy: Int, warning: Int, alert: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("HEALTH OVERVIEW", style = MaterialTheme.typography.labelMedium, color = Primary)
            Text("$count Managed Hives", style = MaterialTheme.typography.headlineLarge)
            val thrivingPercentage = if (count > 0) (healthy * 100 / count) else 0
            Text(
                "$thrivingPercentage% ($healthy/$count) of your colonies are currently thriving. Monitor warnings closely to ensure hive safety.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Row(modifier = Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                StatusIndicator(healthy.toString(), "Healthy", Secondary)
                StatusIndicator(warning.toString(), "Warning", Color(0xFFD97706))
                StatusIndicator(alert.toString(), "Alert", MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Composable
fun StatusIndicator(count: String, label: String, color: Color) {
    Column {
        Text(count, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
fun FilterButton(icon: ImageVector, isActive: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, if (isActive) Primary else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .background(if (isActive) Primary.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = if (isActive) Primary else Color.DarkGray)
    }
}

@Composable
fun HiveCard(hive: Hive, navController: androidx.navigation.NavController, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFF1F3FF), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = Primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(hive.name, style = MaterialTheme.typography.headlineSmall)
                        Text(hive.type, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusIndicator(status = hive.status)
                    if (!hive.isSynced) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.CloudOff, 
                            contentDescription = "Not synced", 
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.CloudDone, 
                            contentDescription = "Synced", 
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricItem("Temperature", hive.temp, modifier = Modifier.weight(1f), isError = hive.tempAlert)
                MetricItem("Humidity", hive.humidity, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Last Inspected: ${hive.lastInspected}", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "History",
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate("inspection_history/${hive.id}") }
                    )
                    Text(
                        "Details", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = Primary, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navController.navigate("edit_hive/${hive.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(status: String, showText: Boolean = true) {
    val (color, label) = when(status) {
        "Healthy" -> Color(0xFF4CAF50) to "Healthy"
        "Warning" -> Color(0xFFFF9800) to "Warning"
        "Alert" -> Color(0xFFF44336) to "Alert"
        else -> Color.Gray to status
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = CircleShape)
        )

        if (showText) {
            Text(label, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, modifier: Modifier = Modifier, isError: Boolean = false) {
    Column(
        modifier = modifier
            .background(Color(0xFFF9F9FF), RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.headlineSmall, color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AddHiveButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFE9EDFF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Add New Hive", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun BottomNavigation(navController: androidx.navigation.NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.9f),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = { 
                if (currentRoute != "dashboard") {
                    navController.navigate("dashboard") { 
                        popUpTo("dashboard") { inclusive = true }
                        launchSingleTop = true 
                    } 
                }
            },
            icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = currentRoute == "alerts",
            onClick = { 
                if (currentRoute != "alerts") {
                    navController.navigate("alerts") { launchSingleTop = true } 
                }
            },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = "Alerts") },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = currentRoute == "inspection",
            onClick = { 
                if (currentRoute != "inspection") {
                    navController.navigate("inspection") { launchSingleTop = true } 
                }
            },
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Inspect") },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = currentRoute == "harvest",
            onClick = { 
                if (currentRoute != "harvest") {
                    navController.navigate("harvest") { launchSingleTop = true } 
                }
            },
            icon = { Icon(Icons.Default.Inventory2, contentDescription = "Harvest") },
            alwaysShowLabel = false
        )
    }
}
