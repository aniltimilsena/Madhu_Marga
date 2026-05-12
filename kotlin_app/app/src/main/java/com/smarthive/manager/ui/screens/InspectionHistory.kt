package com.smarthive.manager.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthive.manager.data.Hive
import com.smarthive.manager.data.HiveViewModel
import com.smarthive.manager.data.Inspection
import com.smarthive.manager.ui.theme.*

// Design tokens matching the HTML spec
private val SurfaceContainerHigh = Color(0xFFE1E8FD)
private val SurfaceContainerLowest = Color(0xFFFFFFFF)
private val OutlineVariant = Color(0xFFDBC2B0)
private val Outline = Color(0xFF887364)
private val TertiaryFixed = Color(0xFFEDE3B8)
private val OnTertiaryFixedVariant = Color(0xFF4D4727)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF93000A)
private val OnSecondaryContainer = Color(0xFF306D58)
private val OrganicShadow = Color(0x14064E3B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionHistoryScreen(
    navController: androidx.navigation.NavController,
    hiveId: Int,
    viewModel: HiveViewModel = viewModel()
) {
    // Real-time data from Room DB
    val allInspections by viewModel.allInspections.collectAsState(initial = emptyList())
    val allHives by viewModel.allHives.collectAsState(initial = emptyList())

    // Filter state
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "Healthy", "Warnings", "Treatments")

    // Build hive lookup map for displaying hive names
    val hiveMap = remember(allHives) { allHives.associateBy { it.id } }

    // Apply filter logic on real data
    val filteredInspections = remember(allInspections, selectedFilter) {
        val sorted = allInspections.sortedByDescending { it.date }
        when (selectedFilter) {
            "Healthy" -> sorted.filter {
                it.healthIssues.contains("Healthy", ignoreCase = true)
            }
            "Warnings" -> sorted.filter {
                !it.healthIssues.contains("Healthy", ignoreCase = true) &&
                !it.healthIssues.contains("Varroa", ignoreCase = true) &&
                !it.healthIssues.contains("Treatment", ignoreCase = true) &&
                !it.healthIssues.contains("Nosema", ignoreCase = true)
            }
            "Treatments" -> sorted.filter {
                it.healthIssues.contains("Varroa", ignoreCase = true) ||
                it.healthIssues.contains("Treatment", ignoreCase = true) ||
                it.healthIssues.contains("Nosema", ignoreCase = true)
            }
            else -> sorted
        }
    }

    Scaffold(
        // TopAppBar matching the HTML header
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inspection History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("inspection/$hiveId") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Inspection", tint = Primary)
                    }
                    IconButton(
                        onClick = { navController.navigate("profile") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = { BottomNavigation(navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp)
        ) {
            // Filter Chips Section
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filterOptions) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChipButton(
                            label = filter,
                            isSelected = isSelected,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }
            }

            // Empty state
            if (filteredInspections.isEmpty()) {
                item {
                    EmptyHistoryState(
                        selectedFilter = selectedFilter,
                        onAddClick = { navController.navigate("inspection/$hiveId") }
                    )
                }
            }

            // Inspection Cards
            items(filteredInspections, key = { it.id }) { inspection ->
                val hive = hiveMap[inspection.hiveId]
                InspectionCard(
                    inspection = inspection,
                    hiveName = hive?.name ?: "Hive #${inspection.hiveId}",
                    hiveHumidity = hive?.humidity ?: "--",
                    viewModel = viewModel,
                    onViewDetails = {
                        navController.navigate("inspection_history/${inspection.hiveId}")
                    }
                )
            }
        }
    }
}

// ─── Filter Chip ─────────────────────────────────────────────
@Composable
private fun FilterChipButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) Primary else SurfaceContainerHigh,
        contentColor = if (isSelected) OnPrimary else OnSurfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Empty State ─────────────────────────────────────────────
@Composable
private fun EmptyHistoryState(selectedFilter: String, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SecondaryContainer.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (selectedFilter != "All") Icons.Default.SearchOff else Icons.Default.Assignment,
                    contentDescription = null, tint = Secondary, modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (selectedFilter != "All") "No \"$selectedFilter\" inspections found"
                else "No inspections recorded yet",
                style = MaterialTheme.typography.titleMedium, color = OnSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (selectedFilter != "All") "Try selecting a different filter."
                else "Log your first inspection to start tracking hive health.",
                style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant
            )
            if (selectedFilter == "All") {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Inspection")
                }
            }
        }
    }
}

// ─── Inspection Card (matching HTML layout) ──────────────────
@Composable
private fun InspectionCard(
    inspection: Inspection,
    hiveName: String,
    hiveHumidity: String,
    viewModel: HiveViewModel,
    onViewDetails: () -> Unit
) {
    val healthStatus = viewModel.calculateStatus(
        inspection.temperament.toString(),
        inspection.healthIssues
    )

    // Determine badge style matching HTML design
    val hasTreatment = inspection.healthIssues.contains("Varroa", true) ||
        inspection.healthIssues.contains("Treatment", true) ||
        inspection.healthIssues.contains("Nosema", true)

    val badgeLabel: String
    val badgeBg: Color
    val badgeTextColor: Color
    val badgeIcon: ImageVector

    when {
        hasTreatment -> {
            badgeLabel = "Action Taken"
            badgeBg = ErrorContainer
            badgeTextColor = OnErrorContainer
            badgeIcon = Icons.Default.MedicalServices
        }
        healthStatus == "Healthy" -> {
            badgeLabel = "Healthy"
            badgeBg = SecondaryContainer
            badgeTextColor = OnSecondaryContainer
            badgeIcon = Icons.Default.CheckCircle
        }
        else -> {
            badgeLabel = "Warning"
            badgeBg = TertiaryFixed
            badgeTextColor = OnTertiaryFixedVariant
            badgeIcon = Icons.Default.Warning
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // ── Header: Date + Hive Name + Badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        inspection.date.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Outline,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        hiveName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Status badge
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            badgeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = badgeTextColor
                        )
                        Text(
                            badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Metrics Row: Temp + Humidity ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .border(
                        BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.2f)),
                        RoundedCornerShape(0.dp)
                    )
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                MetricRow(
                    icon = Icons.Default.Thermostat,
                    label = "Temp",
                    value = String.format("%.1f°C", inspection.temperament)
                )
                MetricRow(
                    icon = Icons.Default.WaterDrop,
                    label = "Humidity",
                    value = hiveHumidity
                )
            }

            // ── Notes Section ──
            if (inspection.notes.isNotBlank()) {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        "Notes",
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        inspection.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp
                    )
                }
            }

            // ── View Details Button ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "View Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─── Metric Item (Temp / Humidity row) ───────────────────────
@Composable
private fun MetricRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
