package com.madhumarga.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val title: String = "Beekeeper",
    val yearsExperience: Int = 0,
    val notificationsEnabled: Boolean = true,
    val membershipStatus: String = "Free Plan",
    val isLoggedIn: Boolean = false
)
