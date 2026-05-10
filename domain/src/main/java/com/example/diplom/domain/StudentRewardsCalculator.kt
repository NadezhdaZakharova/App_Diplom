package com.example.diplom.domain

import com.example.diplom.domain.model.ExerciseCompletionSnapshot
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WorkoutSessionSnapshot
import java.time.LocalDate

private const val TRAINER_KIND = "TRAINER"

object StudentRewardsCalculator {
    fun buildStats(
        events: List<ExerciseCompletionSnapshot>,
        sessions: List<WorkoutSessionSnapshot>,
        firstInstallDateIso: String,
        today: LocalDate,
        calendarWeekStart: LocalDate,
        stepsByDateIso: Map<String, Int>,
        dailyGoal: Int
    ): StudentRewardsStats {
        val firstWeekTarget = 3
        val varietyTarget = 3
        val weekEnd = calendarWeekStart.plusDays(6)
        val monthStart = today.withDayOfMonth(1)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

        val weekEvents = events.filter { rewardsDateInRange(it.dateIso, calendarWeekStart, weekEnd) }
        val topEntry = weekEvents
            .groupingBy { it.exerciseId }
            .eachCount()
            .maxByOrNull { it.value }
        val topSnapshot = topEntry?.let { entry ->
            weekEvents.firstOrNull { it.exerciseId == entry.key }
        }

        val sessionDatesThisWeek = sessions
            .map { it.dateIso }
            .filter { rewardsDateInRange(it, calendarWeekStart, weekEnd) }
            .toSet()
        val marathonComplete = (0L..6L).all { offset ->
            calendarWeekStart.plusDays(offset).toString() in sessionDatesThisWeek
        }

        val monthCount = sessions.count { rewardsDateInRange(it.dateIso, monthStart, monthEnd) }

        val allSessionDates = sessions.map { it.dateIso }.toSet()
        val streakDays = computeWorkoutStreakDays(allSessionDates, stepsByDateIso, dailyGoal, today)

        val installDate = runCatching {
            LocalDate.parse(firstInstallDateIso.ifBlank { today.toString() })
        }.getOrDefault(today)
        val firstWeekEnd = installDate.plusDays(6)
        val firstWeekCount = sessions.count { s ->
            rewardsDateInRange(s.dateIso, installDate, firstWeekEnd)
        }

        val distinctExercisesWeek = weekEvents.map { it.exerciseId }.distinct().size
        val completedTrainer = sessions.any { it.workoutKind == TRAINER_KIND }

        return StudentRewardsStats(
            weekTopExerciseTitleKey = topSnapshot?.titleKey,
            weekTopExerciseTitleFallback = topSnapshot?.title,
            weekTopExerciseCount = topEntry?.value ?: 0,
            weeklyMarathonComplete = marathonComplete,
            workoutDaysThisWeek = sessionDatesThisWeek.size,
            workoutSessionsThisMonth = monthCount,
            daysInMonth = monthEnd.dayOfMonth,
            currentStreakDays = streakDays,
            streakUnlocked3 = streakDays >= 3,
            streakUnlocked7 = streakDays >= 7,
            streakUnlocked14 = streakDays >= 14,
            streakUnlocked30 = streakDays >= 30,
            firstWeekWorkoutsCount = firstWeekCount,
            firstWeekTarget = firstWeekTarget,
            firstWeekComplete = firstWeekCount >= firstWeekTarget,
            varietyDistinctExercises = distinctExercisesWeek,
            varietyTarget = varietyTarget,
            varietyComplete = distinctExercisesWeek >= varietyTarget,
            completedTrainerWorkout = completedTrainer
        )
    }
}

private fun computeWorkoutStreakDays(
    sessionDates: Set<String>,
    stepsByDateIso: Map<String, Int>,
    dailyGoal: Int,
    today: LocalDate
): Int {
    var d = today
    var streak = 0
    while (true) {
        val iso = d.toString()
        val hasSession = iso in sessionDates
        val steps = stepsByDateIso[iso] ?: 0
        val stepsKeepStreak = stepsMeetWorkoutStreakAlternative(steps, dailyGoal)
        if (!hasSession && !stepsKeepStreak) break
        streak++
        d = d.minusDays(1)
    }
    return streak
}

private fun rewardsDateInRange(dateIso: String, start: LocalDate, endInclusive: LocalDate): Boolean {
    val d = LocalDate.parse(dateIso)
    return !d.isBefore(start) && !d.isAfter(endInclusive)
}
