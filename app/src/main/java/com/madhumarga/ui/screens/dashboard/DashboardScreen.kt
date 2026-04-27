package com.madhumarga.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madhumarga.data.db.entity.Hive
import com.madhumarga.ui.components.AlertCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToHives: () -> Unit,
    onNavigateToAddHive: () -> Unit,
    onNavigateToHiveDetail: (Long) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val hiveCount by viewModel.hiveCount.collectAsState(initial = 0)
    val totalHarvest by viewModel.totalHarvest.collectAsState(initial = null)
    val recentInspections by viewModel.recentInspections.collectAsState(initial = emptyList())
    val hives by viewModel.hives.collectAsState(initial = emptyList())

    val alerts = viewModel.generateAlerts(recentInspections)
    val criticalCount = alerts.count { it.severity == AlertSeverity.CRITICAL || it.severity == AlertSeverity.DANGER }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF8F00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐝", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Hive Manager", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF3E2723)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddHive,
                containerColor = Color(0xFFFF8F00),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Hive", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Active Fleet Header
            Text(
                text = "ACTIVE FLEET",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9E9E9E),
                letterSpacing = 1.sp
            )
            Text(
                text = "$hiveCount Managed Hives",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )
            Text(
                text = "Across all apiaries. Monitor your colonies\nfor optimal health and conditions.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status pills
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(count = hiveCount, label = "healthy", color = Color(0xFF4CAF50))
                StatusPill(count = 0, label = "warning", color = Color(0xFFFF9800))
                StatusPill(count = criticalCount, label = "critical", color = Color(0xFFE53935))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bee illustration card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐝", fontSize = 64.sp)
                }
            }

            // Critical Alerts
            if (criticalCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "$criticalCount Critical Alerts",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Hives require immediate attention",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToHives,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("View Details", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Apiary Overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Apiary Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
                Text(
                    text = "Sort by: Location ▼",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hives.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐝", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hives added yet",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9E9E9E)
                        )
                        Text(
                            text = "Tap + to add your first hive",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBDBDBD)
                        )
                    }
                }
            } else {
                hives.forEach { hive ->
                    HiveOverviewCard(hive = hive, onClick = { onNavigateToHiveDetail(hive.id) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add New Hive button
            Button(
                onClick = onNavigateToAddHive,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Hive", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StatusPill(count: Int, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("$count", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = color)
    }
}

@Composable
fun HiveOverviewCard(hive: Hive, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hive.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF3E2723)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (hive.status) {
                                "Warning" -> Color(0xFFFFF3E0)
                                "Critical" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFE8F5E9)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = hive.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (hive.status) {
                            "Warning" -> Color(0xFFFF9800)
                            "Critical" -> Color(0xFFE53935)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Temperature", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Text(
                        text = if (hive.temperature != null) "${hive.temperature}°C" else "-- °C",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF3E2723)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Humidity", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    Text(
                        text = if (hive.humidity != null) "${hive.humidity.toInt()}%" else "-- %",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF3E2723)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ((hive.humidity ?: 50.0) / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFFF8F00),
                trackColor = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Type: ${hive.type}",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}
