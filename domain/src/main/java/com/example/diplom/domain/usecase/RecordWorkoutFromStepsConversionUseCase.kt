package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class RecordWorkoutFromStepsConversionUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke() {
        trainingRepository.recordWorkoutFromStepsConversion()
    }
}
