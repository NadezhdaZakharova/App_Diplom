package com.example.diplom.domain.model

data class DailyStats(
    val dateIso: String,
    val steps: Int,
    val activeMinutes: Int,
    val distanceKm: Double
)

data class PlayerProfile(
    val xp: Int,
    val level: Int,
    val streakDays: Int,
    val bestStreakDays: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val unlockedAtIso: String?
)

data class StoryChapter(
    val chapterNumber: Int,
    val title: String,
    val requiredDistanceKm: Double,
    val questSteps: Int,
    val unlocked: Boolean
)

data class WeeklyChallenge(
    val weekStartIso: String,
    val targetSteps: Int,
    val progressSteps: Int,
    val completed: Boolean
)

enum class AppUserMode {
    STUDENT,
    TRAINER
}

data class Exercise(
    val id: Long,
    val title: String,
    val description: String,
    val defaultReps: Int
)

data class WorkoutExercise(
    val id: Long,
    val dateIso: String,
    val exerciseId: Long,
    val title: String,
    val plannedReps: Int,
    val sortOrder: Int
)

data class StudentRewardsStats(
    val weekTopExerciseTitle: String? = null,
    val weekTopExerciseCount: Int = 0,
    val weeklyMarathonComplete: Boolean = false,
    val workoutDaysThisWeek: Int = 0,
    val workoutSessionsThisMonth: Int = 0,
    val daysInMonth: Int = 30,
    val currentStreakDays: Int = 0,
    val streakUnlocked3: Boolean = false,
    val streakUnlocked7: Boolean = false,
    val streakUnlocked14: Boolean = false,
    val streakUnlocked30: Boolean = false,
    val firstWeekWorkoutsCount: Int = 0,
    val firstWeekTarget: Int = 3,
    val firstWeekComplete: Boolean = false,
    val varietyDistinctExercises: Int = 0,
    val varietyTarget: Int = 3,
    val varietyComplete: Boolean = false,
    val completedTrainerWorkout: Boolean = false
)
