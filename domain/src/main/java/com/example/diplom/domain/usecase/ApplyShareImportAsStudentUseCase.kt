package com.example.diplom.domain.usecase

import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

/** Импорт из «Поделиться» и переключение в режим ученика при успехе; навигация — в presentation-слое. */
class ApplyShareImportAsStudentUseCase @Inject constructor(
    private val importWorkoutUseCase: ImportWorkoutUseCase,
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(rawText: String): Result<Unit> {
        val result = importWorkoutUseCase(rawText)
        if (result.isSuccess) {
            trainingRepository.setUserMode(AppUserMode.STUDENT)
        }
        return result
    }
}
