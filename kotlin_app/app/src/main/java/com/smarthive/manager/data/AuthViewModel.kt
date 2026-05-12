package com.smarthive.manager.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabase: io.github.jan.supabase.SupabaseClient,
    private val repository: com.smarthive.manager.data.HiveRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    val isLoggedIn: StateFlow<Boolean> = authState
        .map { it is AuthState.Authenticated }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is io.github.jan.supabase.gotrue.SessionStatus.Authenticated -> {
                        val user = status.session.user
                        if (user != null) {
                            saveUserToLocal(user)
                        }
                        _authState.value = AuthState.Authenticated(user?.id ?: "")
                    }
                    is io.github.jan.supabase.gotrue.SessionStatus.NotAuthenticated -> {
                        _authState.value = AuthState.Unauthenticated
                    }
                    else -> {}
                }
            }
        }
    }

    private fun checkSession() {
        val session = supabase.auth.currentSessionOrNull()
        if (session != null) {
            saveUserToLocal(session.user)
            _authState.value = AuthState.Authenticated(session.user?.id ?: "")
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun saveUserToLocal(user: io.github.jan.supabase.gotrue.user.UserInfo?) {
        if (user == null) return
        viewModelScope.launch {
            // Only create a profile if one doesn't already exist.
            // This prevents overwriting the user's custom name/title on every login.
            val existing = repository.getUserProfileOnce(user.id)
            if (existing == null) {
                // First-time login: seed a minimal profile from auth metadata
                val profile = UserProfile(
                    userId = user.id,
                    name = user.email?.substringBefore("@") ?: "New Beekeeper",
                    email = user.email ?: "",
                    title = "Beekeeper"  // neutral default; user can edit via Edit Profile
                )
                repository.saveUserProfile(profile)
            } else {
                // Already has a profile: only update the email if it changed
                // Preserve name, title, location, experience, imageUri, etc.
                val emailFromAuth = user.email ?: existing.email
                if (existing.email != emailFromAuth) {
                    repository.saveUserProfile(existing.copy(email = emailFromAuth))
                }
            }
        }
    }

    fun signUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }
                checkSession()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        viewModelScope.launch {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
                checkSession()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }
                checkSession()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            supabase.auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
