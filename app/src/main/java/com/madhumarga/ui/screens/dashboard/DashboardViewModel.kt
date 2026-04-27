package com.madhumarga.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.Inspection
import com.madhumarga.data.repository.HarvestRepository
import com.madhumarga.data.repository.HiveRepository
import com.madhumarga.data.repository.InspectionRepository
import com.madhumarga.data.db.entity.Hive
import kotlinx.coroutines.flow.Flow

data class AlertItem(
    val type: String,
    val message: String,
    val severity: AlertSeverity
)

enum class AlertSeverity {
    CRITICAL, DANGER, WARNING, ADVISORY
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val hiveRepository = HiveRepository(db.hiveDao())
    private val inspectionRepository = InspectionRepository(db.inspectionDao())
    private val harvestRepository = HarvestRepository(db.harvestDao())

    val hiveCount: Flow<Int> = hiveRepository.getHiveCount()
    val totalHarvest: Flow<Double?> = harvestRepository.getTotalHarvest()
    val recentInspections: Flow<List<Inspection>> = inspectionRepository.getRecentInspections()
    val hives: Flow<List<Hive>> = hiveRepository.getAllHives()

    fun generateAlerts(inspections: List<Inspection>): List<AlertItem> {
        val alerts = mutableListOf<AlertItem>()
        for (inspection in inspections) {
            if (!inspection.queenPresent) {
                alerts.add(
                    AlertItem(
                        type = "Critical",
                        message = "Queen absent in hive #${inspection.hiveId}",
                        severity = AlertSeverity.CRITICAL
                    )
                )
            }
            if (inspection.activityLevel == "Low") {
                alerts.add(
                    AlertItem(
                        type = "Warning",
                        message = "Low activity detected in hive #${inspection.hiveId}",
                        severity = AlertSeverity.WARNING
                    )
                )
            }
            if (inspection.pestsPresent) {
                alerts.add(
                    AlertItem(
                        type = "Danger",
                        message = "Pests detected in hive #${inspection.hiveId}",
                        severity = AlertSeverity.DANGER
                    )
                )
            }
            if (inspection.honeyFlow == "Low") {
                alerts.add(
                    AlertItem(
                        type = "Advisory",
                        message = "Low honey flow in hive #${inspection.hiveId}",
                        severity = AlertSeverity.ADVISORY
                    )
                )
            }
        }
        return alerts
    }
}
