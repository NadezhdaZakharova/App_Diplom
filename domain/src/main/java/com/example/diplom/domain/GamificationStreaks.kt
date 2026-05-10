package com.example.diplom.domain

import com.example.diplom.domain.model.DailyStepRecord
import java.time.LocalDate

internal fun calculateCurrentStreak(activity: List<DailyStepRecord>, dailyGoal: Int): Int {
    if (activity.isEmpty()) return 0
    val byDate = activity.associateBy { LocalDate.parse(it.dateIso) }
    var streak = 0
    var cursor = LocalDate.now()
    while (true) {
        val day = byDate[cursor] ?: break
        if (!stepsMeetWorkoutStreakAlternative(day.steps, dailyGoal)) break
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}

internal fun calculateBestStreak(activity: List<DailyStepRecord>, dailyGoal: Int): Int {
    var best = 0
    var current = 0
    activity.sortedBy { it.dateIso }.forEach {
        if (stepsMeetWorkoutStreakAlternative(it.steps, dailyGoal)) {
            current++
            best = maxOf(best, current)
        } else {
            current = 0
        }
    }
    return best
}
