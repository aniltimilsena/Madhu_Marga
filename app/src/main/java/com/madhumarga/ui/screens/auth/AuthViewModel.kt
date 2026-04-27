package com.madhumarga.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.UserProfile
import com.madhumarga.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoggedIn: Boolean = false,
    val authChecked: Boolean = false,
    val loginError: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = UserProfileRepository(db.userProfileDao())

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            val profile = repository.getProfile().first()
            _state.value = _state.value.copy(
                isLoggedIn = profile?.isLoggedIn == true,
                authChecked = true
            )
        }
    }

    fun onNameChange(v: String) { _state.value = _state.value.copy(name = v, nameError = null) }
    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v, emailError = null, loginError = null) }
    fun onPasswordChange(v: String) { _state.value = _state.value.copy(password = v, passwordError = null, loginError = null) }
    fun onConfirmPasswordChange(v: String) { _state.value = _state.value.copy(confirmPassword = v, confirmPasswordError = null) }

    fun signUp() {
        val s = _state.value
        var hasError = false

        if (s.name.isBlank()) {
            _state.value = _state.value.copy(nameError = "Name is required")
            hasError = true
        }
        if (s.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            _state.value = _state.value.copy(emailError = "Valid email is required")
            hasError = true
        }
        if (s.password.length < 6) {
            _state.value = _state.value.copy(passwordError = "Password must be at least 6 characters")
            hasError = true
        }
        if (s.password != s.confirmPassword) {
            _state.value = _state.value.copy(confirmPasswordError = "Passwords do not match")
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            repository.upsertProfile(
                UserProfile(
                    name = s.name.trim(),
                    email = s.email.trim(),
                    password = s.password,
                    isLoggedIn = true
                )
            )
            _state.value = _state.value.copy(isLoggedIn = true)
        }
    }

    fun login() {
        val s = _state.value
        var hasError = false

        if (s.email.isBlank()) {
            _state.value = _state.value.copy(emailError = "Email is required")
            hasError = true
        }
        if (s.password.isBlank()) {
            _state.value = _state.value.copy(passwordError = "Password is required")
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            val profile = repository.getProfile().first()
            if (profile != null && profile.email == s.email && profile.password == s.password) {
                repository.upsertProfile(profile.copy(isLoggedIn = true))
                _state.value = _state.value.copy(isLoggedIn = true)
            } else {
                _state.value = _state.value.copy(loginError = "Invalid email or password")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val profile = repository.getProfile().first()
            if (profile != null) {
                repository.upsertProfile(profile.copy(isLoggedIn = false))
            }
            _state.value = AuthState(authChecked = true)
        }
    }
}
