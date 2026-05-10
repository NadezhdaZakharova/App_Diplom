package com.example.diplom.domain

/**
 * Подготовка текста импорта тренировки (обрезка, выделение JSON, проверка структуры).
 * Реализация живёт в слое data.
 */
fun interface TrainerWorkoutImportPreprocessor {
    fun preparePayload(raw: String): Result<String>
}
