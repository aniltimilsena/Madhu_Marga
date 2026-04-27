package com.madhumarga.ui.screens.inspection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madhumarga.ui.components.AlertCard
import com.madhumarga.ui.screens.dashboard.AlertItem
import com.madhumarga.ui.screens.dashboard.AlertSeverity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspectionScreen(
    hiveId: Long,
    onNavigateBack: () -> Unit,
    viewModel: InspectionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Colony Temperament", "Activity", "Describe")

    LaunchedEffect(state.isSaved) { if (state.isSaved) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFF8F00)),
                            contentAlignment = Alignment.Center
                        ) { Text("🐝", fontSize = 14.sp) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hive Manager")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            Text(
                text = "New Inspection Log",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF3E2723),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF4CAF50)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Queen Presence
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (state.queenPresent) Color(0xFF4CAF50) else Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.queenPresent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Queen Presence", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                    }
                    Switch(
                        checked = state.queenPresent,
                        onCheckedChange = viewModel::onQueenPresentChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Assessment
            Text("HEALTH ASSESSMENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9E9E9E), letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            val healthOptions = listOf("Healthy", "Pests", "Nosema", "Starving", "SHB", "Treatment")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                healthOptions.forEach { option ->
                    val isSelected = state.healthAssessment == option || 
                        (option == "Pests" && state.pestsPresent)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    when (option) {
                                        "Healthy" -> Color(0xFF4CAF50)
                                        "Pests", "Nosema", "Starving", "SHB" -> Color(0xFFE53935)
                                        "Treatment" -> Color(0xFF2196F3)
                                        else -> Color(0xFF4CAF50)
                                    }
                                } else Color.White
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                viewModel.onHealthAssessmentChange(option)
                                if (option == "Pests") viewModel.onPestsPresentChange(true)
                                else if (option == "Healthy") viewModel.onPestsPresentChange(false)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF666666)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Level
            Text("Activity Level", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Medium", "High").forEach { level ->
                    val isSelected = state.activityLevel == level
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFF8F00) else Color.White)
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
                            .clickable { viewModel.onActivityLevelChange(level) }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(level, fontSize = 14.sp, color = if (isSelected) Color.White else Color(0xFF666666), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Honey Flow
            Text("Honey Flow", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Low", "Good").forEach { flow ->
                    val isSelected = state.honeyFlow == flow
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFFF8F00) else Color.White)
                            .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
                            .clickable { viewModel.onHoneyFlowChange(flow) }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(flow, fontSize = 14.sp, color = if (isSelected) Color.White else Color(0xFF666666), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inspection Notes
            Text("Inspection Notes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("Observation notes, colony behavior...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Alert Preview
            val previewAlerts = generatePreviewAlerts(state)
            if (previewAlerts.isNotEmpty()) {
                Text("Alert Preview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                Spacer(modifier = Modifier.height(6.dp))
                previewAlerts.forEach { alert ->
                    AlertCard(alert = alert)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Complete Inspection button
            Button(
                onClick = { viewModel.saveInspection(hiveId) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Inspection", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

private fun generatePreviewAlerts(state: InspectionFormState): List<AlertItem> {
    val alerts = mutableListOf<AlertItem>()
    if (!state.queenPresent) {
        alerts.add(AlertItem("Critical", "Queen absent - immediate attention needed!", AlertSeverity.CRITICAL))
    }
    if (state.activityLevel == "Low") {
        alerts.add(AlertItem("Warning", "Low activity detected", AlertSeverity.WARNING))
    }
    if (state.pestsPresent) {
        alerts.add(AlertItem("Danger", "Pests detected - take action!", AlertSeverity.DANGER))
    }
    if (state.honeyFlow == "Low") {
        alerts.add(AlertItem("Advisory", "Low honey flow observed", AlertSeverity.ADVISORY))
    }
    return alerts
}
