package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class AddExerciseToBankUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(title: String, description: String, defaultDurationSeconds: Int) {
        trainingRepository.addExercise(title, description, defaultDurationSeconds)
    }
}
