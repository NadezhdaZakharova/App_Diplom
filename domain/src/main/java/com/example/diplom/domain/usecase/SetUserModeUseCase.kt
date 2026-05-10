package com.example.diplom.domain.usecase

import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class SetUserModeUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(mode: AppUserMode) {
        trainingRepository.setUserMode(mode)
    }
}
