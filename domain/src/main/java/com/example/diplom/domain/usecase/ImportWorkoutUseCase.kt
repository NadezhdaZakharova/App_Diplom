package com.example.diplom.domain.usecase

import com.example.diplom.domain.TrainerWorkoutImportPreprocessor
import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

/**
 * Импорт тренировки тренера из текста (в т.ч. «Поделиться»).
 * Нормализация и проверка JSON — в [TrainerWorkoutImportPreprocessor] (data).
 */
class ImportWorkoutUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val preprocessor: TrainerWorkoutImportPreprocessor
) {
    suspend operator fun invoke(payload: String): Result<Unit> {
        val normalized = preprocessor.preparePayload(payload).getOrElse { return Result.failure(it) }
        return trainingRepository.importTrainerWorkoutFromJson(normalized)
    }
}
