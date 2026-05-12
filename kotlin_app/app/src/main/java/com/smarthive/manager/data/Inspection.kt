package com.smarthive.manager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "inspections", indices = [androidx.room.Index(value = ["userId"])])
@Serializable
data class Inspection(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiveId: Int,
    val date: String,
    val temperament: Float,
    val queenPresence: Boolean,
    val healthIssues: String, // Comma separated tags
    val notes: String,
    val userId: String = "",
    val isSynced: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
)
