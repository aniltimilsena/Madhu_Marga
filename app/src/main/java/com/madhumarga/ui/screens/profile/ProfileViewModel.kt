package com.madhumarga.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.UserProfile
import com.madhumarga.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val name: String = "",
    val email: String = "",
    val notificationsEnabled: Boolean = true,
    val nameError: String? = null,
    val emailError: String? = null,
    val isSaved: Boolean = false,
    val isLoaded: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = UserProfileRepository(db.userProfileDao())

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getProfile().collect { profile ->
                if (profile != null && !_state.value.isLoaded) {
                    _state.value = _state.value.copy(
                        name = profile.name,
                        email = profile.email,
                        notificationsEnabled = profile.notificationsEnabled,
                        isLoaded = true
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _state.value = _state.value.copy(name = name, nameError = null, isSaved = false)
    }

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, emailError = null, isSaved = false)
    }

    fun onNotificationsToggle(enabled: Boolean) {
        _state.value = _state.value.copy(notificationsEnabled = enabled, isSaved = false)
    }

    fun saveProfile() {
        val currentState = _state.value
        var hasError = false

        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(nameError = "Name cannot be empty")
            hasError = true
        }

        if (currentState.email.isBlank()) {
            _state.value = _state.value.copy(emailError = "Email cannot be empty")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.value = _state.value.copy(emailError = "Enter a valid email address")
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            repository.upsertProfile(
                UserProfile(
                    name = currentState.name.trim(),
                    email = currentState.email.trim(),
                    notificationsEnabled = currentState.notificationsEnabled
                )
            )
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
