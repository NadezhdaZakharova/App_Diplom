package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.ActivityRepository
import com.example.diplom.domain.repository.GamificationRepository
import javax.inject.Inject

class AddStepsUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val gamificationRepository: GamificationRepository
) {
    suspend operator fun invoke(steps: Int) {
        activityRepository.addSteps(steps)
        gamificationRepository.recalculate()
    }
}
