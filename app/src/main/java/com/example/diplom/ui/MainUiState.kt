package com.example.diplom.ui

import com.example.diplom.domain.DEFAULT_DAILY_GOAL_STEPS
import com.example.diplom.domain.GamificationEngine
import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WeeklyChallenge
import com.example.diplom.domain.model.WorkoutExercise

data class DiplomAppNavigationState(
    val showModePicker: Boolean = true,
    val currentDestination: AppDestinations = AppDestinations.TRAINING,
    val sessionActive: Boolean = false,
    val sessionItems: List<WorkoutExercise> = emptyList(),
    val sessionTitle: String = "Тренировка",
    val sessionInstanceId: Int = 0,
    val sessionFromTrainer: Boolean = false,
    /** Одноразовый запрос: переключить ученика на блок импорта «От тренера». */
    val openStudentTrainerSection: Boolean = false
)

data class MainUiState(
    val today: DailyStats = DailyStats("", 0, 0, 0.0),
    val recentDays: List<DailyStats> = emptyList(),
    val dailyGoal: Int = DEFAULT_DAILY_GOAL_STEPS,
    val profile: PlayerProfile = PlayerProfile(0, 1, 0, 0),
    val weeklyChallenge: WeeklyChallenge = WeeklyChallenge("", 55000, 0, false),
    val achievements: List<Achievement> = emptyList(),
    val studentRewards: StudentRewardsStats = StudentRewardsStats(),
    val showStepsToWorkoutConversion: Boolean = false,
    val userMode: AppUserMode = AppUserMode.STUDENT,
    val exerciseBank: List<Exercise> = emptyList(),
    val selfWorkout: List<WorkoutExercise> = emptyList(),
    val trainerWorkout: List<WorkoutExercise> = emptyList(),
    val exportedJson: String = "",
    val importNotification: ImportTransferNotification? = null,
    /** Монотонно растёт при каждом новом баннере — для [LaunchedEffect] без дублирования при повторе того же sealed-объекта. */
    val importNotificationToken: Long = 0L,
    val openStudentTrainerSection: Boolean = false
) {
    /** До 100% при шагах ≥ 1,5× дневной цели (как серия в наградах и XP). */
    val goalProgressFraction: Float
        get() {
            if (dailyGoal <= 0) return 0f
            val denom = dailyGoal * 1.5f
            return (today.steps / denom).coerceIn(0f, 1f)
        }

    val levelProgressFraction: Float
        get() = GamificationEngine.levelProgressFraction(profile.xp)
}
