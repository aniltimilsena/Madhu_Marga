package com.madhumarga.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hives")
data class Hive(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String,
    val apiaryId: Long? = null,
    val temperature: Double? = null,
    val humidity: Double? = null,
    val weight: Double? = null,
    val status: String = "Healthy",
    val createdAt: Long = System.currentTimeMillis()
)
