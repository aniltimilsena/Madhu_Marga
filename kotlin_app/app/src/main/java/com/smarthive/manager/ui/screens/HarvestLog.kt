package com.smarthive.manager.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarthive.manager.ui.theme.Primary
import com.smarthive.manager.ui.theme.Secondary
import com.smarthive.manager.ui.theme.OnSurfaceVariant
import kotlinx.coroutines.launch

import androidx.lifecycle.viewmodel.compose.viewModel
import com.smarthive.manager.data.Harvest
import com.smarthive.manager.data.Hive
import com.smarthive.manager.data.HiveViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.smarthive.manager.data.UserProfile
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestLogScreen(
    navController: androidx.navigation.NavController,
    viewModel: HiveViewModel = viewModel()
) {
    val harvests by viewModel.allHarvests.collectAsState(initial = emptyList())
    val allHives by viewModel.allHives.collectAsState(initial = emptyList())
    val profile by viewModel.userProfile.collectAsState(initial = UserProfile())
    val snackbarHostState = remember { SnackbarHostState() }

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
                            if (currentProfile?.imageUri != null) {
                                AsyncImage(
                                    model = currentProfile.imageUri,
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
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
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
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Yield Progress
            val totalWeight by viewModel.totalHarvestWeight.collectAsState()
            YieldProgressSection(totalWeight)
            
            // Harvest Entry Form
            HarvestEntryForm(hives = allHives) { harvest ->
                viewModel.insertHarvest(harvest)
                // Stay on harvest screen after logging; do NOT pop back
            }
            
            // Recent Harvests Section
            var harvestToDelete by remember { mutableStateOf<Harvest?>(null) }
            
            if (harvestToDelete != null) {
                AlertDialog(
                    onDismissRequest = { harvestToDelete = null },
                    title = { Text("Delete Record?") },
                    text = { Text("Do you want to permanently delete this harvest record?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                harvestToDelete?.let { viewModel.deleteHarvest(it) }
                                harvestToDelete = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { harvestToDelete = null }) { Text("Cancel") }
                    }
                )
            }

            RecentHarvestsSection(harvests) { harvest ->
                harvestToDelete = harvest
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun YieldProgressSection(totalWeight: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* Show detailed stats */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("%.1f kg Total".format(totalWeight), style = MaterialTheme.typography.headlineMedium, color = Primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    val target = 500.0
                    val percentage = (totalWeight / target * 100).coerceAtMost(100.0)
                    Text("%.1f%%".format(percentage), style = MaterialTheme.typography.headlineSmall, color = Secondary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFFFDCC3))
            ) {
                val target = 500.0
                val progressFraction = (totalWeight / target).toFloat().coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFFB77D), Color(0xFFB15F00))))
                )
                Text(
                    "HONEY FLOW LEVEL", 
                    modifier = Modifier.align(Alignment.Center), 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestEntryForm(hives: List<Hive> = emptyList(), onLog: (Harvest) -> Unit) {
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var weight by remember { mutableStateOf("") }
    var variety by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(2) }

    var selectedHive by remember(hives) { mutableStateOf(hives.firstOrNull()) }
    var hiveDropdownExpanded by remember { mutableStateOf(false) }
    
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Keep selected hive in sync when hives list loads after initial composition
    LaunchedEffect(hives) {
        if (selectedHive == null && hives.isNotEmpty()) {
            selectedHive = hives.first()
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Primary)
                Text("New Harvest Record", style = MaterialTheme.typography.headlineSmall, color = Secondary)
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // ── Hive Picker ────────────────────────────────────────────
                if (hives.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB15F00), modifier = Modifier.size(18.dp))
                            Text(
                                "No hives found. Add a hive from the Dashboard first.",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF664D00)
                            )
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = hiveDropdownExpanded,
                        onExpandedChange = { hiveDropdownExpanded = !hiveDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedHive?.name ?: "Select a hive",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Hive") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hiveDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = hiveDropdownExpanded,
                            onDismissRequest = { hiveDropdownExpanded = false }
                        ) {
                            hives.forEach { hive ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(hive.name, style = MaterialTheme.typography.bodyMedium)
                                            Text(hive.type, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        selectedHive = hive
                                        hiveDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Hive, contentDescription = null, tint = Primary)
                                    }
                                )
                            }
                        }
                    }
                }
                // ───────────────────────────────────────────────────────────

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Harvest Date") },
                    placeholder = { Text("e.g. 2024-05-15", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Honey Weight") },
                        placeholder = { Text("e.g. 15.5", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = "kg",
                        onValueChange = { },
                        label = { Text("Unit") },
                        modifier = Modifier.width(80.dp),
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text("Variety / Floral Source") },
                    placeholder = { Text("e.g. Wildflower, Clover", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = moisture,
                    onValueChange = { moisture = it },
                    label = { Text("Moisture Content (%)") },
                    placeholder = { Text("e.g. 17.5", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                    trailingIcon = { Icon(Icons.Default.WaterDrop, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Honey Color Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Honey Color (Pfund Scale)", style = MaterialTheme.typography.labelMedium)
                val colors = listOf(
                    Color(0xFFFFF9E3), Color(0xFFF8E1A0), Color(0xFFE9B94D), 
                    Color(0xFFC88B2D), Color(0xFF9E5D1B), Color(0xFF6B3A0E), Color(0xFF442107)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), 
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    colors.forEachIndexed { index, color ->
                        ColorBox(color, selected = selectedColor == index) {
                            selectedColor = index
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (isSaving) return@Button
                    
                    if (weight.isBlank()) {
                        android.widget.Toast.makeText(context, "Please enter honey weight", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    scope.launch {
                        isSaving = true
                        val harvest = Harvest(
                            hiveId = selectedHive?.id ?: 0,
                            date = date,
                            weight = weight,
                            variety = variety,
                            moisture = moisture,
                            colorIndex = selectedColor
                        )
                        onLog(harvest)
                        kotlinx.coroutines.delay(1000)
                        isSaving = false
                        weight = "" // clear form
                        variety = ""
                        moisture = ""
                    }
                },
                enabled = hives.isNotEmpty() && !isSaving,   // Disabled when no hives exist or saving
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Log Harvest Record")
            }
        }
    }
}

@Composable
fun ColorBox(color: Color, selected: Boolean = false, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(if (selected) 2.dp else 1.dp, if (selected) Primary else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
    )
}

@Composable
fun RecentHarvestsSection(harvests: List<Harvest>, onDelete: (Harvest) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Recent Harvests", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (harvests.isEmpty()) {
                Text("No harvests logged yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                harvests.take(5).forEach { harvest ->
                    val colors = listOf(
                        Color(0xFFFFF9E3), Color(0xFFF8E1A0), Color(0xFFE9B94D), 
                        Color(0xFFC88B2D), Color(0xFF9E5D1B), Color(0xFF6B3A0E), Color(0xFF442107)
                    )
                    val indicatorColor = if (harvest.colorIndex in colors.indices) colors[harvest.colorIndex] else Primary
                    HarvestHistoryItem(harvest.date, harvest.variety, "${harvest.weight}kg", indicatorColor) {
                        onDelete(harvest)
                    }
                }
            }
        }
    }
}

@Composable
fun HarvestHistoryItem(date: String, type: String, weight: String, colorIndicator: Color, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* View record details */ },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sleek Vertical Color Indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(colorIndicator)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(date, style = MaterialTheme.typography.labelLarge)
            Text(type, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
        
        Text(weight, style = MaterialTheme.typography.headlineSmall, color = Primary)

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
        }
    }
}
