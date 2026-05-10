package com.example.diplom.domain

import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.DailyStepRecord
import com.example.diplom.domain.model.WeeklyChallenge

object GamificationEngine {
    fun toDistanceKm(steps: Int): Double = (steps * 0.00075).coerceAtLeast(0.0)

    fun calculatePlayerProgress(activity: List<DailyStepRecord>, dailyGoal: Int): PlayerProgress {
        if (activity.isEmpty()) {
            return PlayerProgress(xp = 0, level = 1, streakDays = 0, bestStreakDays = 0)
        }

        val sorted = activity.sortedBy { it.dateIso }
        var xp = 0
        sorted.forEach { day ->
            val baseXp = (day.steps / 100).coerceAtMost(DAILY_XP_CAP)
            val goalBonus = if (stepsMeetWorkoutStreakAlternative(day.steps, dailyGoal)) {
                GOAL_BONUS_XP
            } else {
                0
            }
            xp += baseXp + goalBonus
        }

        val streak = calculateCurrentStreak(sorted, dailyGoal)
        val bestStreak = calculateBestStreak(sorted, dailyGoal)
        val streakMultiplier = (1.0 + (streak.coerceAtMost(10) * 0.025)).coerceAtMost(STREAK_MULTIPLIER_MAX)
        xp = (xp * streakMultiplier).toInt()

        return PlayerProgress(
            xp = xp,
            level = xpToLevel(xp),
            streakDays = streak,
            bestStreakDays = bestStreak
        )
    }

    fun xpToLevel(xp: Int): Int = (xp / 300) + 1

    fun levelProgressFraction(xp: Int): Float {
        val levelBase = (xp / 300) * 300
        return ((xp - levelBase) / 300f).coerceIn(0f, 1f)
    }

    fun initialAchievements(): List<Achievement> = initialAchievementsData()

    fun updateAchievements(
        existing: List<Achievement>,
        activity: List<DailyStepRecord>,
        dailyGoal: Int,
        todayIso: String
    ): List<Achievement> {
        val totalSteps = activity.sumOf { it.steps }
        val daysWithGoal = activity.count { stepsMeetWorkoutStreakAlternative(it.steps, dailyGoal) }
        val has1000InDay = activity.any { it.steps >= 1000 }
        val streak = calculateCurrentStreak(activity.sortedBy { it.dateIso }, dailyGoal)

        return existing.map { achievement ->
            val unlockedNow = when (achievement.id) {
                "first_steps" -> has1000InDay
                "goal_crusher" -> daysWithGoal >= 3
                "trail_runner" -> totalSteps >= 50_000
                "streak_7" -> streak >= 7
                else -> achievement.unlocked
            }
            if (achievement.unlocked || !unlockedNow) {
                achievement
            } else {
                achievement.copy(unlocked = true, unlockedAtIso = todayIso)
            }
        }
    }

    fun buildWeeklyChallenge(
        existing: WeeklyChallenge?,
        weekStartIso: String,
        weekSteps: Int
    ): WeeklyChallenge {
        val target = 55_000
        return if (existing == null || existing.weekStartIso != weekStartIso) {
            WeeklyChallenge(
                weekStartIso = weekStartIso,
                targetSteps = target,
                progressSteps = weekSteps,
                completed = weekSteps >= target
            )
        } else {
            existing.copy(
                progressSteps = weekSteps,
                completed = weekSteps >= existing.targetSteps
            )
        }
    }
}
