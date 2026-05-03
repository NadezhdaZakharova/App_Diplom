package com.example.diplom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_challenge")
data class WeeklyChallengeEntity(
    @PrimaryKey val id: Int = 0,
    val weekStartIso: String,
    val targetSteps: Int,
    val progressSteps: Int,
    val completed: Boolean
)
