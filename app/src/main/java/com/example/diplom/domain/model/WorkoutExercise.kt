package com.example.diplom.domain.model

data class WorkoutExercise(
    val id: Long,
    val dateIso: String,
    val exerciseId: Long,
    val title: String,
    val description: String = "",
    val plannedReps: Int,
    val sortOrder: Int
)
