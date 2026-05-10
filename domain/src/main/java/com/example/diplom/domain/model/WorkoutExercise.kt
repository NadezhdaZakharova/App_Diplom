package com.example.diplom.domain.model

/**
 * Пункт плана тренировки. [plannedReps] — запланированное время выполнения в секундах (имя поля историческое).
 */
data class WorkoutExercise(
    val id: Long,
    val dateIso: String,
    val exerciseId: Long,
    val title: String,
    val description: String = "",
    val plannedReps: Int,
    val sortOrder: Int,
    val titleKey: String? = null
)
