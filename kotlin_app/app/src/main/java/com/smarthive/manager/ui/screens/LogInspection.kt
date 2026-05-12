package com.smarthive.manager.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarthive.manager.ui.theme.Primary
import com.smarthive.manager.ui.theme.Secondary
import com.smarthive.manager.ui.theme.OnSurfaceVariant

import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthive.manager.data.HiveViewModel
import com.smarthive.manager.data.Inspection
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.smarthive.manager.data.HiveImage
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import com.smarthive.manager.data.UserProfile
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInspectionScreen(
    navController: androidx.navigation.NavController,
    hiveId: Int,
    viewModel: HiveViewModel = viewModel()
) {
    val allHives by viewModel.allHives.collectAsState()
    val allInspections by viewModel.allInspections.collectAsState()
    val profile by viewModel.userProfile.collectAsState(initial = UserProfile())
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var selectedHiveId by remember { mutableIntStateOf(hiveId) }
    var expanded by remember { mutableStateOf(false) }
    val selectedHive = allHives.find { it.id == selectedHiveId }
    
    // Track photos taken in THIS session before inspection is saved
    val sessionPhotos = remember { mutableStateListOf<HiveImage>() }
    val sessionPhotoIds = remember { mutableStateListOf<Int>() }
    
    val images by viewModel.getImagesForHive(selectedHiveId).collectAsState(initial = emptyList())
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    try {
                        val inputStream = context.contentResolver.openInputStream(it)
                        val fileName = "inspection_${selectedHiveId}_${System.currentTimeMillis()}.jpg"
                        val file = java.io.File(context.filesDir, fileName)
                        inputStream?.use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        val date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())
                        val newImage = HiveImage(
                            hiveId = selectedHiveId,
                            inspectionId = null, // Linked later
                            imageUri = file.toURI().toString(),
                            date = date
                        )
                        viewModel.insertHiveImage(newImage) { generatedId ->
                            sessionPhotoIds.add(generatedId)
                        }
                        sessionPhotos.add(newImage) // Track it for UI feedback
                        snackbarHostState.showSnackbar("Photo added to current inspection!")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error saving photo: ${e.message}")
                    }
                }
            }
        }
    )

    var selectedInspectionForDetail by remember { mutableStateOf<Inspection?>(null) }
    
    if (selectedInspectionForDetail != null) {
        InspectionDetailDialog(
            inspection = selectedInspectionForDetail!!,
            viewModel = viewModel,
            onDismiss = { selectedInspectionForDetail = null }
        )
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
                            val photoUri = currentProfile?.imageUri
                            if (photoUri != null) {
                                AsyncImage(
                                    model = photoUri,
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
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hive Selector
            Column {
                Text("Selecting Hive for Inspection", style = MaterialTheme.typography.labelMedium, color = Primary)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedHive?.name ?: "Select Hive",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allHives.forEach { hive ->
                            DropdownMenuItem(
                                text = { Text(hive.name) },
                                onClick = {
                                     selectedHiveId = hive.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Vitals Section
            val hive = selectedHive
            if (hive == null) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "No hives found. Please add a hive from the Dashboard to begin inspecting.",
                            color = Primary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { navController.navigate("dashboard") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go to Dashboard")
                        }
                    }
                }
            } else {
                Text("Current Vitals: ${hive.name}", style = MaterialTheme.typography.headlineSmall, color = Secondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VitalCard("Temperature", hive.temp, modifier = Modifier.weight(1f))
                    VitalCard("Humidity", hive.humidity, modifier = Modifier.weight(1f))
                    VitalCard("Status", hive.status, modifier = Modifier.weight(1f))
                }
            }

            // Inspection Form
            InspectionForm(
                hiveId = selectedHiveId, 
                onComplete = { inspection ->
                    viewModel.insertInspection(inspection) { newId ->
                        // Link photos to this inspection
                        sessionPhotoIds.forEach { imgId ->
                            viewModel.updateImageInspectionId(imgId, newId)
                        }
                        sessionPhotos.clear()
                        sessionPhotoIds.clear()
                        scope.launch { snackbarHostState.showSnackbar("✅ Inspection saved successfully!") }
                    }
                }
            )
            
            // Photo Log
            PhotoLog(
                images = images,
                onAddPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onPhotoClick = { /* No details modal yet, keeping functional gallery */ },
                onDeletePhoto = { image ->
                    viewModel.deleteHiveImage(image)
                    scope.launch { snackbarHostState.showSnackbar("Photo removed.") }
                }
            )
            
            // Inspection History
            val hiveInspections = allInspections.filter { it.hiveId == selectedHiveId }.sortedByDescending { it.id }
            if (hiveInspections.isNotEmpty()) {
                Text("Inspection History", style = MaterialTheme.typography.headlineSmall, color = Secondary)
                hiveInspections.forEach { inspection ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedInspectionForDetail = inspection },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(inspection.date, style = MaterialTheme.typography.labelMedium, color = Primary)
                                if (!inspection.queenPresence) {
                                    Text("No Queen", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                                }
                            }
                            Text(inspection.notes, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun VitalCard(label: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun InspectionForm(hiveId: Int, onComplete: (Inspection) -> Unit) {
    var temperament by remember { mutableStateOf(0.4f) }
    var queenPresence by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    var healthIssues by remember { mutableStateOf(setOf("Healthy")) }
    
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("New Inspection Log", style = MaterialTheme.typography.headlineSmall, color = Secondary)
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
            }

            // Temperament
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Colony Temperament", style = MaterialTheme.typography.labelMedium)
                    Text(if (temperament < 0.3f) "Aggressive" else if (temperament < 0.7f) "Gentle" else "Very Calm", style = MaterialTheme.typography.bodyMedium, color = Primary, fontWeight = FontWeight.Bold)
                }
                Slider(value = temperament, onValueChange = { temperament = it }, colors = SliderDefaults.colors(thumbColor = Primary, activeTrackColor = Primary))
            }

            // Queen Presence
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Queen Presence", style = MaterialTheme.typography.labelMedium)
                }
                Switch(checked = queenPresence, onCheckedChange = { queenPresence = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Primary))
            }

            // Health Assessment
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Health Assessment", style = MaterialTheme.typography.labelMedium)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Healthy", "Varroa", "Nosema", "Chalkbrood").forEach { label ->
                        val isSelected = healthIssues.contains(label)
                        HealthTag(label, isSelected) {
                            healthIssues = if (isSelected) {
                                healthIssues - label
                            } else {
                                healthIssues + label
                            }
                        }
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Inspection notes...", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (isSaving) return@Button
                    
                    if (notes.isBlank()) {
                        android.widget.Toast.makeText(context, "Please enter inspection notes", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    scope.launch {
                        isSaving = true
                        val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                        val inspection = Inspection(
                            hiveId = hiveId,
                            date = date,
                            temperament = temperament,
                            queenPresence = queenPresence,
                            healthIssues = healthIssues.joinToString(", "),
                            notes = notes
                        )
                        onComplete(inspection)
                        kotlinx.coroutines.delay(1000)
                        isSaving = false
                        // Reset all fields
                        notes = "" 
                        healthIssues = setOf("Healthy")
                        temperament = 0.4f
                        queenPresence = true
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Complete Inspection")
            }
        }
    }
}

@Composable
fun HealthTag(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
        border = BorderStroke(1.dp, if (selected) Color(0xFF2E7D32) else Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(vertical = 4.dp).clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = selected, onCheckedChange = { onClick() }, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PhotoLog(images: List<HiveImage>, onAddPhoto: () -> Unit, onPhotoClick: (HiveImage) -> Unit, onDeletePhoto: (HiveImage) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Recent Photo Log", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            images.forEach { image ->
                PhotoItem(
                    image = image,
                    onDeleteClick = { onDeletePhoto(image) },
                    onClick = { onPhotoClick(image) }
                )
            }
            AddPhotoItem(onClick = onAddPhoto)
        }
    }
}

@Composable
fun PhotoItem(image: HiveImage, onDeleteClick: () -> Unit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.BottomStart
    ) {
        AsyncImage(
            model = image.imageUri,
            contentDescription = "Hive Photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(8.dp)
        ) {
            Text(image.date, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Delete Photo", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AddPhotoItem(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 140.dp, height = 180.dp)
            .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
            Text("Add Photo", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(modifier: Modifier, horizontalArrangement: Arrangement.Horizontal, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(modifier = modifier, horizontalArrangement = horizontalArrangement) {
        content()
    }
}

@Composable
fun InspectionDetailDialog(inspection: Inspection, viewModel: HiveViewModel, onDismiss: () -> Unit) {
    val inspectionImages by viewModel.getImagesForInspection(inspection.id).collectAsState(initial = emptyList())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inspection Details - ${inspection.date}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow("Temperament", if (inspection.temperament < 0.3f) "Aggressive" else if (inspection.temperament < 0.7f) "Gentle" else "Very Calm")
                DetailRow("Queen Presence", if (inspection.queenPresence) "Yes" else "No")
                DetailRow("Health Issues", inspection.healthIssues)
                
                Text("Notes:", style = MaterialTheme.typography.labelMedium, color = Primary)
                Text(inspection.notes, style = MaterialTheme.typography.bodyMedium)
                
                if (inspectionImages.isNotEmpty()) {
                    Text("Photos:", style = MaterialTheme.typography.labelMedium, color = Primary)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        inspectionImages.forEach { image ->
                            AsyncImage(
                                model = image.imageUri,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
