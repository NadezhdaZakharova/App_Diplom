package com.example.diplom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activity")
data class DailyActivityEntity(
    @PrimaryKey val dateIso: String,
    val steps: Int,
    val activeMinutes: Int
)
