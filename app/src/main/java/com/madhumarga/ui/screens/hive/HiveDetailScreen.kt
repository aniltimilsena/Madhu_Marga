package com.madhumarga.ui.screens.hive

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.madhumarga.data.db.entity.HiveImage
import com.madhumarga.data.db.entity.Inspection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDetailScreen(
    hiveId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToInspection: (Long) -> Unit,
    onNavigateToHarvest: (Long) -> Unit,
    viewModel: HiveDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(hiveId) { viewModel.loadHive(hiveId) }
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onNavigateBack() }

    val inspections by viewModel.inspections.collectAsState(initial = emptyList())
    val harvests by viewModel.harvests.collectAsState(initial = emptyList())
    val images by viewModel.images.collectAsState(initial = emptyList())
    val totalHarvest by viewModel.totalHarvest.collectAsState(initial = null)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.addImage(hiveId, it.toString()) } }

    val hive = state.hive

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hive?.name ?: "Hive Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { hive?.let { onNavigateToEdit(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF3E2723),
                    navigationIconContentColor = Color(0xFF3E2723),
                    actionIconContentColor = Color(0xFF3E2723)
                )
            )
        }
    ) { padding ->
        if (hive == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Loading...") }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp)
            ) {
                // Current Vitals
                Text(
                    text = "Current Vitals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Temperature Card
                VitalCard(
                    label = "TEMPERATURE",
                    value = if (hive.temperature != null) "${hive.temperature}°C" else "-- °C",
                    subtitle = "Optimal Range",
                    progress = ((hive.temperature ?: 35.0) / 50.0).toFloat().coerceIn(0f, 1f),
                    color = Color(0xFFFF8F00),
                    icon = { Icon(Icons.Default.Thermostat, contentDescription = null, tint = Color(0xFFFF8F00), modifier = Modifier.size(20.dp)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Humidity Card
                VitalCard(
                    label = "HUMIDITY",
                    value = if (hive.humidity != null) "${hive.humidity.toInt()}%" else "-- %",
                    subtitle = null,
                    progress = ((hive.humidity ?: 60.0) / 100.0).toFloat().coerceIn(0f, 1f),
                    color = Color(0xFF2196F3),
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Weight Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL WEIGHT", fontSize = 11.sp, color = Color(0xFF9E9E9E), letterSpacing = 1.sp)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (hive.weight != null) "${hive.weight} kg" else "-- kg",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📈 Gain in progress", fontSize = 12.sp, color = Color(0xFF4CAF50))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateToInspection(hiveId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("New Inspection", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onNavigateToHarvest(hiveId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00))
                    ) {
                        Text("Add Harvest", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Photo Log
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RECENT PHOTO LOG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9E9E9E), letterSpacing = 1.sp)
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (images.isEmpty()) {
                    Text("No photos yet", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBDBDBD))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(images, key = { it.id }) { image ->
                            PhotoItem(image = image, onDelete = { viewModel.deleteImage(image.id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Inspection History
                Text("Inspection History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                Spacer(modifier = Modifier.height(8.dp))

                if (inspections.isEmpty()) {
                    Text("No inspections recorded yet.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBDBDBD))
                } else {
                    inspections.forEach { inspection ->
                        InspectionHistoryCard(inspection)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Harvest History
                Text("Harvest History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                Text("Total: ${totalHarvest ?: 0.0} kg", fontSize = 14.sp, color = Color(0xFFFF8F00), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                if (harvests.isEmpty()) {
                    Text("No harvests recorded yet.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFBDBDBD))
                } else {
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    harvests.forEach { harvest ->
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
                                    Text(dateFormat.format(Date(harvest.date)), fontSize = 13.sp, color = Color(0xFF9E9E9E))
                                    Text(harvest.variety, fontSize = 12.sp, color = Color(0xFFBDBDBD))
                                }
                                Text("${harvest.quantityKg} kg", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFF8F00))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.deleteHive() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Hive", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun VitalCard(
    label: String,
    value: String,
    subtitle: String?,
    progress: Float,
    color: Color,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E), letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
            if (subtitle != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(subtitle, fontSize = 12.sp, color = Color(0xFF4CAF50))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun PhotoItem(image: HiveImage, onDelete: () -> Unit) {
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Uri.parse(image.imageUri))
                .crossfade(true)
                .build(),
            contentDescription = "Hive photo",
            modifier = Modifier.size(110.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun InspectionHistoryCard(inspection: Inspection) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = inspection.title.ifBlank { "Inspection" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF3E2723)
                )
                Text(dateFormat.format(Date(inspection.date)), fontSize = 12.sp, color = Color(0xFF9E9E9E))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InspectionStatusItem("Queen", if (inspection.queenPresent) "Present" else "Absent", inspection.queenPresent)
                InspectionStatusItem("Activity", inspection.activityLevel, inspection.activityLevel != "Low")
                InspectionStatusItem("Pests", if (inspection.pestsPresent) "Yes" else "No", !inspection.pestsPresent)
            }
            if (inspection.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Notes: ${inspection.notes}", fontSize = 12.sp, color = Color(0xFF9E9E9E))
            }
        }
    }
}

@Composable
fun InspectionStatusItem(label: String, value: String, isGood: Boolean) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isGood) Color(0xFF4CAF50) else Color(0xFFE53935))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, color = Color(0xFF9E9E9E))
        }
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF3E2723))
    }
}
