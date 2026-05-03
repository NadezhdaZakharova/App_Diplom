package com.example.diplom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val dailyGoal: Int = 8000,
    val mode: String = "STUDENT",
    val firstInstallDateIso: String = ""
)
