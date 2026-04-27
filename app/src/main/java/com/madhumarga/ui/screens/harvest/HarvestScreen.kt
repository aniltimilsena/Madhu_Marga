package com.madhumarga.ui.screens.harvest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    hiveId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HarvestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val harvests by viewModel.harvests.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(hiveId) { viewModel.loadHarvests(hiveId) }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            snackbarHostState.showSnackbar("Record Saved Successfully!")
            viewModel.resetSaveState()
        }
    }

    // Calculate seasonal goal progress
    val totalHarvested = harvests.sumOf { it.quantityKg }
    val seasonalGoal = 500.0
    val progressPercent = ((totalHarvested / seasonalGoal) * 100).coerceAtMost(100.0)

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            // Seasonal Goal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SEASONAL GOAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF8F00), letterSpacing = 1.sp)
                        Text("TARGET: ${seasonalGoal.toInt()} KG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9E9E9E))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${String.format("%.1f", totalHarvested)} kg", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("/day", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (progressPercent / 100).toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF8F00),
                        trackColor = Color(0xFFFFE0B2)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${String.format("%.1f", progressPercent)}%", fontSize = 12.sp, color = Color(0xFFFF8F00), modifier = Modifier.align(Alignment.End))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // New Harvest Record Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🍯 New Harvest Record", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Honey Weight
                    Text("Honey Weight", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = state.quantityText,
                        onValueChange = viewModel::onQuantityChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.0") },
                        suffix = { Text("kg") },
                        isError = state.quantityError != null,
                        supportingText = state.quantityError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF8F00),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Variety / Floral Source
                    Text("Variety / Floral Source", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Wildflower", "Clover", "Manuka", "Acacia").forEach { variety ->
                            val isSelected = state.variety == variety
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFFFF8F00) else Color.White)
                                    .border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.onVarietyChange(variety) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(variety, fontSize = 12.sp, color = if (isSelected) Color.White else Color(0xFF666666))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Moisture Content
                    Text("Moisture Content (%)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = state.moistureContent,
                            onValueChange = viewModel::onMoistureChange,
                            valueRange = 10f..25f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF8F00),
                                activeTrackColor = Color(0xFFFF8F00)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${String.format("%.1f", state.moistureContent)}%", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Honey Color
                    Text("Honey Color (Pfund Scale)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF666666))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Water White" to Color(0xFFFFF9C4),
                            "Amber" to Color(0xFFFFCC02),
                            "Dark Amber" to Color(0xFFD4790E)
                        ).forEach { (name, color) ->
                            val isSelected = state.honeyColor == name
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.onHoneyColorChange(name) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFFF8F00) else Color(0xFFE0E0E0),
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, fontSize = 10.sp, color = Color(0xFF666666))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.saveHarvest(hiveId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("Log Harvest Record", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(0.6f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Tip
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HIVE HEALTH TIP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"Ensure moisture content is below 18% to prevent fermentation during storage. High-quality wildflower honey often peaks at 17%.\"",
                        fontSize = 13.sp,
                        color = Color(0xFF388E3C),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last Harvests
            Text("Last 3 Harvests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            Spacer(modifier = Modifier.height(8.dp))

            if (harvests.isEmpty()) {
                Text("No harvests recorded yet.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBDBDBD))
            } else {
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                harvests.take(3).forEach { harvest ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dateFormat.format(Date(harvest.date)), fontSize = 14.sp, color = Color(0xFF666666))
                                Text(harvest.variety, fontSize = 12.sp, color = Color(0xFFBDBDBD))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🍯", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${harvest.quantityKg} kg", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFF8F00))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
