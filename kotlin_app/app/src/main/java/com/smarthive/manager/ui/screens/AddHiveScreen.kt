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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.smarthive.manager.data.Hive
import com.smarthive.manager.data.HiveViewModel
import com.smarthive.manager.ui.theme.Primary
import kotlinx.coroutines.launch
import com.smarthive.manager.utils.rememberClickEnabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHiveScreen(navController: NavController, viewModel: HiveViewModel = viewModel()) {
    var hiveName by remember { mutableStateOf("") }
    var hiveType by remember { mutableStateOf("") }
    var hiveTemp by remember { mutableStateOf("") }
    var hiveHumidity by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Add New Hive") },
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
            Text("Enter the details for your new hive to begin tracking.", style = MaterialTheme.typography.bodyLarge)
            
            OutlinedTextField(
                value = hiveName,
                onValueChange = { 
                    hiveName = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Hive Name") },
                placeholder = { Text("e.g. Hive #42", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("Hive name cannot be empty", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            
            OutlinedTextField(
                value = hiveType,
                onValueChange = { hiveType = it },
                label = { Text("Hive Type") },
                placeholder = { Text("e.g. Top Bar, Langstroth", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = hiveTemp,
                    onValueChange = { hiveTemp = it },
                    label = { Text("Temp (°C)") },
                    placeholder = { Text("e.g. 34.5", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = hiveHumidity,
                    onValueChange = { hiveHumidity = it },
                    label = { Text("Humidity (%)") },
                    placeholder = { Text("e.g. 55", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val (clickEnabled, onBtnClick) = rememberClickEnabled()

            Button(
                onClick = {
                    if (!clickEnabled) return@Button
                    val tempVal = hiveTemp.toDoubleOrNull()
                    val humidVal = hiveHumidity.toDoubleOrNull()
                    when {
                        hiveName.isBlank() -> nameError = true
                        hiveTemp.isNotBlank() && (tempVal == null || tempVal < 0 || tempVal > 60) -> {
                            scope.launch { snackbarHostState.showSnackbar("❌ Temperature must be between 0–60°C") }
                        }
                        hiveHumidity.isNotBlank() && (humidVal == null || humidVal < 0 || humidVal > 100) -> {
                            scope.launch { snackbarHostState.showSnackbar("❌ Humidity must be between 0–100%") }
                        }
                        else -> {
                            onBtnClick()
                            try {
                                val status = viewModel.calculateStatus(hiveTemp)
                                val newHive = Hive(
                                    name = hiveName,
                                    type = hiveType,
                                    status = status,
                                    temp = if (hiveTemp.isBlank()) "–" else "${hiveTemp}°C",
                                    humidity = if (hiveHumidity.isBlank()) "–" else "${hiveHumidity}%",
                                    lastInspected = "Today"
                                )
                                viewModel.insertHive(newHive)
                                navController.popBackStack()
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error saving hive: ${e.message}")
                                }
                            }
                        }
                    }
                },
                enabled = clickEnabled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (clickEnabled) {
                    Text("Save Hive")
                } else {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
