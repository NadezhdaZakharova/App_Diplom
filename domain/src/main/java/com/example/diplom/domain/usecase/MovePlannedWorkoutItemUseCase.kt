package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class MovePlannedWorkoutItemUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(id: Long, moveDown: Boolean) {
        trainingRepository.moveWorkoutItem(id, moveDown)
    }
}
