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
    val healthAssessment: String = "Healthy",
    val isSaved: Boolean = false
)

class InspectionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = InspectionRepository(db.inspectionDao())

    private val _state = MutableStateFlow(InspectionFormState())
    val state: StateFlow<InspectionFormState> = _state.asStateFlow()

    fun onQueenPresentChange(v: Boolean) { _state.value = _state.value.copy(queenPresent = v) }
    fun onActivityLevelChange(v: String) { _state.value = _state.value.copy(activityLevel = v) }
    fun onPestsPresentChange(v: Boolean) { _state.value = _state.value.copy(pestsPresent = v) }
    fun onHoneyFlowChange(v: String) { _state.value = _state.value.copy(honeyFlow = v) }
    fun onNotesChange(v: String) { _state.value = _state.value.copy(notes = v) }
    fun onHealthAssessmentChange(v: String) { _state.value = _state.value.copy(healthAssessment = v) }

    fun saveInspection(hiveId: Long) {
        val s = _state.value
        viewModelScope.launch {
            repository.insertInspection(
                Inspection(
                    hiveId = hiveId,
                    queenPresent = s.queenPresent,
                    activityLevel = s.activityLevel,
                    pestsPresent = s.pestsPresent,
                    honeyFlow = s.honeyFlow,
                    notes = s.notes,
                    healthAssessment = s.healthAssessment
                )
            )
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
