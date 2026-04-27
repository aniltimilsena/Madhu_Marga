package com.madhumarga.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.madhumarga.ui.screens.dashboard.AlertItem
import com.madhumarga.ui.screens.dashboard.AlertSeverity
import com.madhumarga.ui.theme.BlueInfo
import com.madhumarga.ui.theme.OrangeWarning
import com.madhumarga.ui.theme.RedAlert

@Composable
fun AlertCard(alert: AlertItem, modifier: Modifier = Modifier) {
    val containerColor = when (alert.severity) {
        AlertSeverity.CRITICAL -> RedAlert.copy(alpha = 0.1f)
        AlertSeverity.DANGER -> RedAlert.copy(alpha = 0.08f)
        AlertSeverity.WARNING -> OrangeWarning.copy(alpha = 0.1f)
        AlertSeverity.ADVISORY -> BlueInfo.copy(alpha = 0.1f)
    }

    val iconTint = when (alert.severity) {
        AlertSeverity.CRITICAL, AlertSeverity.DANGER -> RedAlert
        AlertSeverity.WARNING -> OrangeWarning
        AlertSeverity.ADVISORY -> BlueInfo
    }

    val icon = when (alert.severity) {
        AlertSeverity.CRITICAL, AlertSeverity.DANGER -> Icons.Default.Error
        AlertSeverity.WARNING -> Icons.Default.Warning
        AlertSeverity.ADVISORY -> Icons.Default.Info
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = alert.type,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = alert.type,
                    style = MaterialTheme.typography.labelLarge,
                    color = iconTint
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
