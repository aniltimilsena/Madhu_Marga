package com.smarthive.manager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "harvests", indices = [androidx.room.Index(value = ["userId"])])
@Serializable
data class Harvest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiveId: Int,
    val date: String,
    val weight: String,
    val variety: String,
    val moisture: String,
    val colorIndex: Int,
    val userId: String = "",
    val isSynced: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
)
