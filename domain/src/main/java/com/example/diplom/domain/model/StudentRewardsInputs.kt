package com.example.diplom.domain.model

/** Снимок события «упражнение отмечено» для расчёта наград ученика. */
data class ExerciseCompletionSnapshot(
    val dateIso: String,
    val exerciseId: Long,
    val title: String,
    val titleKey: String? = null
)

/** Снимок завершённой тренировки (день + тип плана). */
data class WorkoutSessionSnapshot(
    val dateIso: String,
    val workoutKind: String
)
