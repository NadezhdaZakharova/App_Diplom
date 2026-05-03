package com.example.diplom.domain.model

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
