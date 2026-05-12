package com.smarthive.manager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "hive_images",
    indices = [
        androidx.room.Index(value = ["userId"]),
        androidx.room.Index(value = ["hiveId"]),
        androidx.room.Index(value = ["inspectionId"])
    ]
)
@Serializable
data class HiveImage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiveId: Int,
    val inspectionId: Int? = null,
    val imageUri: String,
    val date: String,
    val userId: String = "",
    val isSynced: Boolean = false
)
