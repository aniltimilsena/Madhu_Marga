package com.madhumarga.ui.screens.hive

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madhumarga.data.db.entity.Hive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddHive: () -> Unit,
    onNavigateToEditHive: (Long) -> Unit,
    onNavigateToHiveDetail: (Long) -> Unit,
    viewModel: HiveListViewModel = viewModel()
) {
    val hives by viewModel.hives.collectAsState(initial = emptyList())

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
                        Text("My Hives", fontWeight = FontWeight.Bold)
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
        if (hives.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐝", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No hives added yet", fontWeight = FontWeight.Medium, color = Color(0xFF9E9E9E))
                    Text("Tap + to add your first hive", style = MaterialTheme.typography.bodySmall, color = Color(0xFFBDBDBD))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hives, key = { it.id }) { hive ->
                    HiveCard(
                        hive = hive,
                        onClick = { onNavigateToHiveDetail(hive.id) },
                        onEdit = { onNavigateToEditHive(hive.id) },
                        onDelete = { viewModel.deleteHive(hive) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveCard(
    hive: Hive,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Text("🐝", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hive.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF3E2723))
                Text("Type: ${hive.type}", fontSize = 13.sp, color = Color(0xFF9E9E9E))
                Text("Added: ${dateFormat.format(Date(hive.createdAt))}", fontSize = 11.sp, color = Color(0xFFBDBDBD))
            }
            Row {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (hive.status) {
                                "Warning" -> Color(0xFFFFF3E0)
                                "Critical" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFE8F5E9)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = hive.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (hive.status) {
                            "Warning" -> Color(0xFFFF9800)
                            "Critical" -> Color(0xFFE53935)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFFF8F00), modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
