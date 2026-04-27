package com.madhumarga.ui.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.madhumarga.MadhuMargaApp
import com.madhumarga.data.db.entity.Apiary
import com.madhumarga.data.db.entity.UserProfile
import com.madhumarga.data.repository.ApiaryRepository
import com.madhumarga.data.repository.HarvestRepository
import com.madhumarga.data.repository.HiveRepository
import com.madhumarga.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileState(
    val name: String = "",
    val email: String = "",
    val title: String = "Beekeeper",
    val yearsExperience: Int = 0,
    val activeHives: Int = 0,
    val totalHarvested: Int = 0,
    val notificationsEnabled: Boolean = true,
    val membershipStatus: String = "Free Plan",
    val nameError: String? = null,
    val emailError: String? = null,
    val isSaved: Boolean = false,
    val isLoaded: Boolean = false
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MadhuMargaApp).database
    private val repository = UserProfileRepository(db.userProfileDao())
    private val hiveRepository = HiveRepository(db.hiveDao())
    private val harvestRepository = HarvestRepository(db.harvestDao())
    private val apiaryRepository = ApiaryRepository(db.apiaryDao())

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    val apiaries: Flow<List<Apiary>> = apiaryRepository.getAllApiaries()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = repository.getProfile().first()
            val hiveCount = hiveRepository.getHiveCount().first()
            val totalHarvest = harvestRepository.getTotalHarvest().first()

            if (profile != null) {
                _state.value = _state.value.copy(
                    name = profile.name,
                    email = profile.email,
                    title = profile.title,
                    yearsExperience = profile.yearsExperience,
                    activeHives = hiveCount,
                    totalHarvested = ((totalHarvest ?: 0.0) / 1000).toInt(),
                    notificationsEnabled = profile.notificationsEnabled,
                    membershipStatus = profile.membershipStatus,
                    isLoaded = true
                )
            } else {
                _state.value = _state.value.copy(
                    activeHives = hiveCount,
                    totalHarvested = ((totalHarvest ?: 0.0) / 1000).toInt(),
                    isLoaded = true
                )
            }
        }
    }

    fun onNameChange(v: String) { _state.value = _state.value.copy(name = v, nameError = null) }
    fun onEmailChange(v: String) { _state.value = _state.value.copy(email = v, emailError = null) }

    fun toggleNotifications() {
        val newVal = !_state.value.notificationsEnabled
        _state.value = _state.value.copy(notificationsEnabled = newVal)
        viewModelScope.launch {
            val profile = repository.getProfile().first() ?: UserProfile()
            repository.upsertProfile(profile.copy(notificationsEnabled = newVal))
        }
    }

    fun saveProfile() {
        val s = _state.value
        var hasError = false

        if (s.name.isBlank()) {
            _state.value = _state.value.copy(nameError = "Name is required")
            hasError = true
        }
        if (s.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
            _state.value = _state.value.copy(emailError = "Invalid email format")
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            val existing = repository.getProfile().first()
            val profile = (existing ?: UserProfile()).copy(
                name = s.name.trim(),
                email = s.email.trim(),
                title = s.title,
                yearsExperience = s.yearsExperience,
                notificationsEnabled = s.notificationsEnabled,
                membershipStatus = s.membershipStatus
            )
            repository.upsertProfile(profile)
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
