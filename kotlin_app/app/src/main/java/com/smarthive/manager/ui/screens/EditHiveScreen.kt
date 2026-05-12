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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHiveScreen(navController: NavController, hiveId: Int, viewModel: HiveViewModel = viewModel()) {
    val allHives by viewModel.allHives.collectAsState(initial = emptyList())
    val hive = allHives.find { it.id == hiveId }

    var hiveName by remember { mutableStateOf("") }
    var hiveType by remember { mutableStateOf("") }
    var hiveTemp by remember { mutableStateOf("") }
    var hiveHumidity by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(hive) {
        hive?.let {
            hiveName = it.name
            hiveType = it.type
            hiveTemp = it.temp.replace("°C", "")
            hiveHumidity = it.humidity.replace("%", "")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Hive") },
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
                placeholder = { Text("e.g. Langstroth", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
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

            Button(
                onClick = {
                    if (isSaving) return@Button
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
                        hive != null -> {
                            isSaving = true
                            try {
                                val status = viewModel.calculateStatus(hiveTemp)
                                val updatedHive = hive.copy(
                                    name = hiveName,
                                    type = hiveType,
                                    status = status,
                                    temp = if (hiveTemp.isBlank()) hive.temp else "${hiveTemp}°C",
                                    humidity = if (hiveHumidity.isBlank()) hive.humidity else "${hiveHumidity}%"
                                )
                                viewModel.updateHive(updatedHive)
                                navController.popBackStack()
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error updating hive: ${e.message}")
                                }
                                isSaving = false
                            }
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Update Hive")
                }
            }
            
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete Hive")
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Hive?") },
                    text = { Text("Are you sure you want to permanently delete '${hive?.name}'? This will also remove all its history and logs.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (hive != null) {
                                    viewModel.deleteHive(hive)
                                    navController.popBackStack()
                                }
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}
