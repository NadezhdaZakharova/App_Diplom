package com.example.diplom.domain.usecase

import com.example.diplom.domain.ActivityState
import com.example.diplom.domain.CoreMainUiInputs
import com.example.diplom.domain.GameState
import com.example.diplom.domain.TrainingState
import com.example.diplom.domain.repository.ActivityRepository
import com.example.diplom.domain.repository.GamificationRepository
import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Единая точка подписки на потоки данных для главного экрана.
 * Слой presentation зависит только от этого use case, а не от репозиториев напрямую.
 */
@Singleton
class ObserveCoreMainUiInputsUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val gamificationRepository: GamificationRepository,
    private val trainingRepository: TrainingRepository
) {
    operator fun invoke(): Flow<CoreMainUiInputs> {
        val activityBlock = combine(
            activityRepository.observeToday(),
            activityRepository.observeRecentDays(),
            activityRepository.observeDailyGoal(),
            gamificationRepository.observeProfile()
        ) { today, recent, goal, profile ->
            ActivityState(today, recent, goal, profile)
        }
        val gameBlock = combine(
            gamificationRepository.observeWeeklyChallenge(),
            gamificationRepository.observeAchievements()
        ) { weekly, achievements ->
            GameState(weekly, achievements)
        }
        val trainingBlock = combine(
            trainingRepository.observeUserMode(),
            trainingRepository.observeExerciseBank(),
            trainingRepository.observeSelfWorkoutToday(),
            trainingRepository.observeTrainerWorkoutToday()
        ) { mode, bank, selfWorkout, trainerWorkout ->
            TrainingState(mode, bank, selfWorkout, trainerWorkout)
        }
        return combine(
            activityBlock,
            gameBlock,
            trainingBlock,
            trainingRepository.observeStudentRewardsStats(),
            trainingRepository.observeStepsConversionPromptVisible()
        ) { activity, game, training, studentRewards, showStepsConversion ->
            CoreMainUiInputs(
                activity = activity,
                game = game,
                training = training,
                studentRewards = studentRewards,
                showStepsToWorkoutConversion = showStepsConversion
            )
        }
    }
}
