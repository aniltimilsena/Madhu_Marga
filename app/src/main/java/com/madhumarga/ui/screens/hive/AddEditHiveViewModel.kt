package com.madhumarga.ui.screens.hive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.Hive
import com.madhumarga.data.repository.HiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditHiveState(
    val name: String = "",
    val type: String = "Langstroth",
    val isEditing: Boolean = false,
    val editingHiveId: Long? = null,
    val nameError: String? = null,
    val isSaved: Boolean = false
)

class AddEditHiveViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = HiveRepository(db.hiveDao())

    private val _state = MutableStateFlow(AddEditHiveState())
    val state: StateFlow<AddEditHiveState> = _state.asStateFlow()

    fun loadHive(hiveId: Long) {
        viewModelScope.launch {
            repository.getHiveById(hiveId).collect { hive ->
                if (hive != null) {
                    _state.value = _state.value.copy(
                        name = hive.name,
                        type = hive.type,
                        isEditing = true,
                        editingHiveId = hive.id
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name, nameError = null)
    }

    fun onTypeChange(type: String) {
        _state.value = _state.value.copy(type = type)
    }

    fun saveHive() {
        val currentState = _state.value
        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(nameError = "Hive name cannot be empty")
            return
        }

        viewModelScope.launch {
            if (currentState.isEditing && currentState.editingHiveId != null) {
                repository.updateHive(
                    Hive(
                        id = currentState.editingHiveId,
                        name = currentState.name.trim(),
                        type = currentState.type
                    )
                )
            } else {
                repository.insertHive(
                    Hive(
                        name = currentState.name.trim(),
                        type = currentState.type
                    )
                )
            }
            _state.value = currentState.copy(isSaved = true)
        }
    }
}
