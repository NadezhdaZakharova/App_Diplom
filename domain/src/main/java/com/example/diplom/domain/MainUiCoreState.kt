package com.example.diplom.domain

import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WeeklyChallenge
import com.example.diplom.domain.model.WorkoutExercise

data class ActivityState(
    val today: DailyStats,
    val recent: List<DailyStats>,
    val goal: Int,
    val profile: PlayerProfile
)

data class GameState(
    val weekly: WeeklyChallenge,
    val achievements: List<Achievement>
)

data class TrainingState(
    val mode: AppUserMode,
    val bank: List<Exercise>,
    val selfWorkout: List<WorkoutExercise>,
    val trainerWorkout: List<WorkoutExercise>
)

data class CoreMainUiInputs(
    val activity: ActivityState,
    val game: GameState,
    val training: TrainingState,
    val studentRewards: StudentRewardsStats,
    val showStepsToWorkoutConversion: Boolean
)
