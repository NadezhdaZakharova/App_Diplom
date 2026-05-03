package com.example.diplom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_workout_session")
data class CompletedWorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateIso: String,
    val workoutKind: String = "SELF"
)
