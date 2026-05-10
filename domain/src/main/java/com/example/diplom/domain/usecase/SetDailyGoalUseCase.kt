package com.example.diplom.domain.usecase

import com.example.diplom.domain.repository.ActivityRepository
import javax.inject.Inject

class SetDailyGoalUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    suspend operator fun invoke(steps: Int) {
        activityRepository.setDailyGoal(steps)
    }
}
