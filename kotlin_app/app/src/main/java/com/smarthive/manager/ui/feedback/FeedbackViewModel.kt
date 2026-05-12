package com.smarthive.manager.ui.feedback

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarthive.manager.data.remote.gemini.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: GeminiRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(FeedbackUiState())
    val uiState: State<FeedbackUiState> = _uiState

    fun onInputChanged(input: String) {
        _uiState.value = _uiState.value.copy(input = input)
    }

    fun getFeedback() {
        val currentInput = _uiState.value.input
        if (currentInput.isBlank()) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            repository.getFeedback(currentInput)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        response = response,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Unknown error occurred",
                        isLoading = false
                    )
                }
        }
    }
}

data class FeedbackUiState(
    val input: String = "",
    val response: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
