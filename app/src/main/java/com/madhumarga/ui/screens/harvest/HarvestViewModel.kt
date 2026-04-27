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
import kotlinx.coroutines.launch

data class HarvestFormState(
    val quantityText: String = "",
    val quantityError: String? = null,
    val isSaved: Boolean = false
)

class HarvestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = HarvestRepository(db.harvestDao())

    private val _state = MutableStateFlow(HarvestFormState())
    val state: StateFlow<HarvestFormState> = _state.asStateFlow()

    fun getHarvestsForHive(hiveId: Long): Flow<List<Harvest>> =
        repository.getHarvestsForHive(hiveId)

    fun onQuantityChange(value: String) {
        _state.value = _state.value.copy(quantityText = value, quantityError = null)
    }

    fun saveHarvest(hiveId: Long) {
        val currentState = _state.value
        val quantity = currentState.quantityText.toDoubleOrNull()

        if (quantity == null || quantity <= 0) {
            _state.value = currentState.copy(quantityError = "Enter a valid quantity (> 0)")
            return
        }

        viewModelScope.launch {
            repository.insertHarvest(
                Harvest(hiveId = hiveId, quantityKg = quantity)
            )
            _state.value = HarvestFormState(isSaved = true)
        }
    }

    fun resetState() {
        _state.value = HarvestFormState()
    }
}
