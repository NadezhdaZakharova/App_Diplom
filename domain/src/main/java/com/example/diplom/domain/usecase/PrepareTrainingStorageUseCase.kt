package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

/** Первичная подготовка хранилища тренировок после старта приложения. */
class PrepareTrainingStorageUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke() {
        trainingRepository.ensureFirstInstallDateRecorded()
        trainingRepository.seedExerciseBankIfEmpty()
    }
}
