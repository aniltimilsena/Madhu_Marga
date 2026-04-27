package com.madhumarga.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hive_images",
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
data class HiveImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hiveId: Long,
    val imageUri: String,
    val addedAt: Long = System.currentTimeMillis()
)
