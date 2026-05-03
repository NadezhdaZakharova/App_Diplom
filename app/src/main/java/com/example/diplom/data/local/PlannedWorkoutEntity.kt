package com.example.diplom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planned_workout")
data class PlannedWorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateIso: String,
    val exerciseId: Long,
    val title: String,
    val description: String = "",
    val plannedReps: Int,
    val sortOrder: Int,
    val workoutType: String = "SELF"
)
