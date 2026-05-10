package com.example.diplom.domain.model

/**
 * Упражнение в банке. [defaultReps] — длительность выполнения по умолчанию в секундах (имя поля историческое).
 */
data class Exercise(
    val id: Long,
    val title: String,
    val description: String,
    val defaultReps: Int,
    val titleKey: String? = null
)
