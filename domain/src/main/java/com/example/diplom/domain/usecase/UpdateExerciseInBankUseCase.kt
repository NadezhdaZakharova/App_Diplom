package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class UpdateExerciseInBankUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(
        id: Long,
        title: String,
        description: String,
        defaultDurationSeconds: Int
    ) {
        trainingRepository.updateExercise(id, title, description, defaultDurationSeconds)
    }
}
