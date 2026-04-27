package com.madhumarga.ui.screens.hive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.Harvest
import com.madhumarga.data.db.entity.Hive
import com.madhumarga.data.db.entity.HiveImage
import com.madhumarga.data.db.entity.Inspection
import com.madhumarga.data.repository.HarvestRepository
import com.madhumarga.data.repository.HiveImageRepository
import com.madhumarga.data.repository.HiveRepository
import com.madhumarga.data.repository.InspectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HiveDetailState(
    val hive: Hive? = null,
    val isDeleted: Boolean = false
)

class HiveDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val hiveRepository = HiveRepository(db.hiveDao())
    private val inspectionRepository = InspectionRepository(db.inspectionDao())
    private val harvestRepository = HarvestRepository(db.harvestDao())
    private val imageRepository = HiveImageRepository(db.hiveImageDao())

    private val _state = MutableStateFlow(HiveDetailState())
    val state: StateFlow<HiveDetailState> = _state.asStateFlow()

    lateinit var inspections: Flow<List<Inspection>>
        private set
    lateinit var harvests: Flow<List<Harvest>>
        private set
    lateinit var images: Flow<List<HiveImage>>
        private set
    lateinit var totalHarvest: Flow<Double?>
        private set

    fun loadHive(hiveId: Long) {
        inspections = inspectionRepository.getInspectionsForHive(hiveId)
        harvests = harvestRepository.getHarvestsForHive(hiveId)
        images = imageRepository.getImagesForHive(hiveId)
        totalHarvest = harvestRepository.getTotalHarvestForHive(hiveId)

        viewModelScope.launch {
            hiveRepository.getHiveById(hiveId).collect { hive ->
                _state.value = _state.value.copy(hive = hive)
            }
        }
    }

    fun deleteHive() {
        val hive = _state.value.hive ?: return
        viewModelScope.launch {
            hiveRepository.deleteHive(hive)
            _state.value = _state.value.copy(isDeleted = true)
        }
    }

    fun addImage(hiveId: Long, uri: String) {
        viewModelScope.launch {
            imageRepository.insertImage(HiveImage(hiveId = hiveId, imageUri = uri))
        }
    }

    fun deleteImage(imageId: Long) {
        viewModelScope.launch {
            imageRepository.deleteImage(imageId)
        }
    }
}
