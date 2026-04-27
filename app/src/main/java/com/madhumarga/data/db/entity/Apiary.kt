package com.madhumarga.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apiaries")
data class Apiary(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val hiveCount: Int = 0,
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)
