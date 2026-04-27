package com.madhumarga.ui.screens.inspection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madhumarga.ui.components.AlertCard
import com.madhumarga.ui.screens.dashboard.AlertItem
import com.madhumarga.ui.screens.dashboard.AlertSeverity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionScreen(
    hiveId: Long,
    onNavigateBack: () -> Unit,
    viewModel: InspectionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    // Generate live preview alerts
    val previewAlerts = mutableListOf<AlertItem>()
    if (!state.queenPresent) {
        previewAlerts.add(AlertItem("Critical", "Queen absent - immediate attention needed!", AlertSeverity.CRITICAL))
    }
    if (state.activityLevel == "Low") {
        previewAlerts.add(AlertItem("Warning", "Low activity detected", AlertSeverity.WARNING))
    }
    if (state.pestsPresent) {
        previewAlerts.add(AlertItem("Danger", "Pests detected - take action!", AlertSeverity.DANGER))
    }
    if (state.honeyFlow == "Low") {
        previewAlerts.add(AlertItem("Advisory", "Low honey flow observed", AlertSeverity.ADVISORY))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Inspection") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Hive Inspection",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Queen Present
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Queen Present", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.queenPresent) "Yes" else "No",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = state.queenPresent,
                        onCheckedChange = viewModel::onQueenPresentChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity Level
            Text("Activity Level", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                listOf("Low", "Medium", "High").forEach { level ->
                    FilterChip(
                        selected = state.activityLevel == level,
                        onClick = { viewModel.onActivityLevelChange(level) },
                        label = { Text(level) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pests Present
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pests Present", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.pestsPresent) "Yes" else "No",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = state.pestsPresent,
                        onCheckedChange = viewModel::onPestsPresentChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Honey Flow
            Text("Honey Flow", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                listOf("Low", "Good").forEach { flow ->
                    FilterChip(
                        selected = state.honeyFlow == flow,
                        onClick = { viewModel.onHoneyFlowChange(flow) },
                        label = { Text(flow) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Preview Alerts
            if (previewAlerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Alert Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                previewAlerts.forEach { alert ->
                    AlertCard(alert = alert)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveInspection(hiveId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Inspection")
            }
        }
    }
}
