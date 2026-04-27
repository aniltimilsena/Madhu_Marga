package com.madhumarga.ui.screens.harvest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.Harvest
import com.madhumarga.data.repository.HarvestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class HarvestFormState(
    val quantityText: String = "",
    val variety: String = "Wildflower",
    val moistureContent: Float = 17.0f,
    val honeyColor: String = "Amber",
    val quantityError: String? = null,
    val isSaved: Boolean = false
)

class HarvestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = HarvestRepository(db.harvestDao())

    private val _state = MutableStateFlow(HarvestFormState())
    val state: StateFlow<HarvestFormState> = _state.asStateFlow()

    private val _harvests = MutableStateFlow<Flow<List<Harvest>>>(flowOf(emptyList()))
    val harvests: Flow<List<Harvest>> get() = _harvests.value

    fun loadHarvests(hiveId: Long) {
        _harvests.value = repository.getHarvestsForHive(hiveId)
    }

    fun onQuantityChange(v: String) { _state.value = _state.value.copy(quantityText = v, quantityError = null) }
    fun onVarietyChange(v: String) { _state.value = _state.value.copy(variety = v) }
    fun onMoistureChange(v: Float) { _state.value = _state.value.copy(moistureContent = v) }
    fun onHoneyColorChange(v: String) { _state.value = _state.value.copy(honeyColor = v) }

    fun resetSaveState() {
        _state.value = _state.value.copy(isSaved = false, quantityText = "")
    }

    fun saveHarvest(hiveId: Long) {
        val qty = _state.value.quantityText.toDoubleOrNull()
        if (qty == null || qty <= 0) {
            _state.value = _state.value.copy(quantityError = "Enter a valid quantity greater than 0")
            return
        }
        viewModelScope.launch {
            repository.insertHarvest(
                Harvest(
                    hiveId = hiveId,
                    quantityKg = qty,
                    variety = _state.value.variety,
                    moistureContent = _state.value.moistureContent.toDouble(),
                    honeyColor = _state.value.honeyColor
                )
            )
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
