package com.smarthive.manager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "hives", indices = [androidx.room.Index(value = ["userId"])])
@Serializable
data class Hive(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String,
    val status: String,
    val temp: String,
    val humidity: String,
    val lastInspected: String,
    val tempAlert: Boolean = false,
    val userId: String = "",
    val isSynced: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
)
