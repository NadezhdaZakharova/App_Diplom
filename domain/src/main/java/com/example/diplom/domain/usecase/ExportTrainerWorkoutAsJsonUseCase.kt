package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class ExportTrainerWorkoutAsJsonUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(): String = trainingRepository.exportTrainerWorkoutAsJson()
}
