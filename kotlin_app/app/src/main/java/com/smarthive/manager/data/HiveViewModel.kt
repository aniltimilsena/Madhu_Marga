package com.smarthive.manager.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.smarthive.manager.services.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.smarthive.manager.utils.RateLimiter
import javax.inject.Inject

@HiltViewModel
class HiveViewModel @Inject constructor(
    private val repository: HiveRepository,
    application: Application
) : AndroidViewModel(application) {

    private val insertRateLimiter = RateLimiter(2000L)    // insertHive: 2 seconds
    private val harvestRateLimiter = RateLimiter(2000L)   // insertHarvest: 2 seconds
    private val inspectionRateLimiter = RateLimiter(2000L) // insertInspection: 2 seconds
    private val refreshRateLimiter = RateLimiter(5000L)    // refreshAll: 5 seconds

    val allHives: StateFlow<List<Hive>> = repository.allHives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHarvests: StateFlow<List<Harvest>> = repository.allHarvests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInspections: StateFlow<List<Inspection>> = repository.allInspections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Aggregated Stats
    val healthyCount: StateFlow<Int> = allHives.map { hives -> hives.count { it.status == "Healthy" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val warningCount: StateFlow<Int> = allHives.map { hives -> hives.count { it.status == "Warning" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alertCount: StateFlow<Int> = allHives.map { hives -> hives.count { it.status == "Alert" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalHarvestWeight: StateFlow<Double> = allHarvests.map { harvests -> 
        harvests.sumOf { it.weight.toDoubleOrNull() ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        if (!refreshRateLimiter.canExecute()) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshHives()
                repository.refreshHarvests()
                repository.refreshInspections()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun insertHive(hive: Hive) {
        if (!insertRateLimiter.canExecute()) return
        viewModelScope.launch {
            repository.insertHive(hive)
        }
    }

    fun claimAndSyncOfflineData() {
        viewModelScope.launch {
            repository.claimAndSync()
        }
    }

    fun insertHarvest(harvest: Harvest) {
        if (!harvestRateLimiter.canExecute()) return
        viewModelScope.launch {
            repository.insertHarvest(harvest)
        }
    }

    fun deleteHarvest(harvest: Harvest) {
        viewModelScope.launch {
            repository.deleteHarvest(harvest)
        }
    }

    fun insertInspection(inspection: Inspection, onResult: (Int) -> Unit = {}) {
        if (!inspectionRateLimiter.canExecute()) return
        viewModelScope.launch {
            val id = repository.insertInspection(inspection)
            onResult(id)
        }
    }

    fun updateImageInspectionId(imageId: Int, inspectionId: Int) {
        viewModelScope.launch {
            repository.updateImageInspectionId(imageId, inspectionId)
        }
    }

    fun getImagesForInspection(inspectionId: Int): Flow<List<HiveImage>> {
        return repository.getImagesForInspection(inspectionId)
    }

    fun deleteInspection(inspection: Inspection) {
        viewModelScope.launch {
            repository.deleteInspection(inspection)
        }
    }

    fun getInspectionsForHive(hiveId: Int): Flow<List<Inspection>> {
        return repository.getInspectionsForHive(hiveId)
    }

    fun getHiveByIdFlow(hiveId: Int): Flow<Hive?> {
        return repository.getHiveByIdFlow(hiveId)
    }

    fun updateHive(hive: Hive) {
        viewModelScope.launch {
            repository.updateHive(hive)
        }
    }

    fun deleteHive(hive: Hive) {
        viewModelScope.launch {
            repository.deleteHive(hive)
        }
    }

    // User Profile
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    // Hive Images
    fun getImagesForHive(hiveId: Int): Flow<List<HiveImage>> = repository.getImagesForHive(hiveId)

    fun insertHiveImage(image: HiveImage, onIdGenerated: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertHiveImage(image)
            onIdGenerated(id)
        }
    }

    fun deleteHiveImage(image: HiveImage) {
        viewModelScope.launch {
            repository.deleteHiveImage(image)
        }
    }

    /**
     * Real hive health logic:
     * - Temperature range: 32–35°C is ideal. <28°C or >38°C is Alert. Borders = Warning.
     * - If any health issue besides "Healthy" is present, status is at least Warning.
     * - Alert temp always wins regardless of health issues.
     *
     * @param tempStr  temperature string, may contain "°C" suffix
     * @param healthIssues  comma-separated list from inspection (e.g. "Varroa, Nosema")
     */
    fun calculateStatus(tempStr: String, healthIssues: String = ""): String {
        val temp = tempStr.replace("°C", "").trim().toDoubleOrNull()

        val tempStatus: String = when {
            temp == null -> "Warning"            // unparseable → unknown = caution
            temp > 38.0 || temp < 28.0 -> "Alert"  // danger zone
            temp > 35.0 || temp < 32.0 -> "Warning" // outside ideal brood range
            else -> "Healthy"                    // 32–35°C: ideal brood temperature
        }

        // Pest / disease override — any tag other than "Healthy" means at least Warning
        val hasPestIssue = healthIssues.isNotBlank() &&
            healthIssues.split(",").any { it.trim().isNotEmpty() && it.trim() != "Healthy" }

        return when {
            tempStatus == "Alert" -> "Alert"  // critical temp always takes priority
            hasPestIssue -> if (tempStatus == "Healthy") "Warning" else tempStatus
            else -> tempStatus
        }
    }

    fun getRemindersForHive(hiveId: Int) = repository.getRemindersForHive(hiveId)

    fun insertReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    fun scheduleReminder(reminder: Reminder) {
        val workManager = WorkManager.getInstance(getApplication())
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val scheduledDate = sdf.parse(reminder.date) ?: return
        
        val currentTime = System.currentTimeMillis()
        val delay = scheduledDate.time - currentTime
        
        if (delay > 0) {
            val data = Data.Builder()
                .putString("title", "Beekeeping Follow-up")
                .putString("message", reminder.description)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("reminder_${reminder.id}")
                .build()

            workManager.enqueue(workRequest)
        }
    }
}
