package com.smarthive.manager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders", indices = [androidx.room.Index(value = ["hiveId"])])
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hiveId: Int,
    val date: String,
    val description: String,
    val isCompleted: Boolean = false
)
