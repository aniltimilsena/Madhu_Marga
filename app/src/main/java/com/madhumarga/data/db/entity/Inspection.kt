package com.madhumarga.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inspections",
    foreignKeys = [
        ForeignKey(
            entity = Hive::class,
            parentColumns = ["id"],
            childColumns = ["hiveId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hiveId"])]
)
data class Inspection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hiveId: Long,
    val queenPresent: Boolean,
    val activityLevel: String,
    val pestsPresent: Boolean,
    val honeyFlow: String,
    val notes: String = "",
    val title: String = "",
    val colonyTemperament: String = "",
    val healthAssessment: String = "Healthy",
    val date: Long = System.currentTimeMillis()
)
