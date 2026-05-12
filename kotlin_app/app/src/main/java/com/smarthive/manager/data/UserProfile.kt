package com.smarthive.manager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_profile")
@Serializable
data class UserProfile(
    @PrimaryKey val userId: String = "",
    val name: String = "",
    val title: String = "",
    val email: String = "",
    val location: String = "",
    val experience: String = "",
    
    // Notification Settings
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = false,
    val tempAlerts: Boolean = true,
    val humidityAlerts: Boolean = true,
    val harvestReminders: Boolean = true,
    val marketingEnabled: Boolean = false,
    val imageUri: String? = null
)
