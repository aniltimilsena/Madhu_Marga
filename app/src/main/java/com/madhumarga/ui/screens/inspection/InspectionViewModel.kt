package com.madhumarga.ui.screens.inspection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.Inspection
import com.madhumarga.data.repository.InspectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InspectionFormState(
    val queenPresent: Boolean = true,
    val activityLevel: String = "Medium",
    val pestsPresent: Boolean = false,
    val honeyFlow: String = "Good",
    val notes: String = "",
    val isSaved: Boolean = false
)

class InspectionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = InspectionRepository(db.inspectionDao())

    private val _state = MutableStateFlow(InspectionFormState())
    val state: StateFlow<InspectionFormState> = _state.asStateFlow()

    fun onQueenPresentChange(value: Boolean) {
        _state.value = _state.value.copy(queenPresent = value)
    }

    fun onActivityLevelChange(value: String) {
        _state.value = _state.value.copy(activityLevel = value)
    }

    fun onPestsPresentChange(value: Boolean) {
        _state.value = _state.value.copy(pestsPresent = value)
    }

    fun onHoneyFlowChange(value: String) {
        _state.value = _state.value.copy(honeyFlow = value)
    }

    fun onNotesChange(value: String) {
        _state.value = _state.value.copy(notes = value)
    }

    fun saveInspection(hiveId: Long) {
        val currentState = _state.value
        viewModelScope.launch {
            repository.insertInspection(
                Inspection(
                    hiveId = hiveId,
                    queenPresent = currentState.queenPresent,
                    activityLevel = currentState.activityLevel,
                    pestsPresent = currentState.pestsPresent,
                    honeyFlow = currentState.honeyFlow,
                    notes = currentState.notes
                )
            )
            _state.value = currentState.copy(isSaved = true)
        }
    }
}
