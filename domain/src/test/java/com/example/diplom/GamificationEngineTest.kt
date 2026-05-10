package com.example.diplom

import com.example.diplom.domain.DEFAULT_DAILY_GOAL_STEPS
import com.example.diplom.domain.GamificationEngine
import com.example.diplom.domain.model.DailyStepRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamificationEngineTest {
    @Test
    fun xpAndLevelAreCalculated() {
        val history = listOf(
            DailyStepRecord("2026-02-20", 9000, 90),
            DailyStepRecord("2026-02-21", 8500, 85),
            DailyStepRecord("2026-02-22", 4000, 40)
        )
        val progress = GamificationEngine.calculatePlayerProgress(history, dailyGoal = DEFAULT_DAILY_GOAL_STEPS)
        assertTrue(progress.xp > 0)
        assertTrue(progress.level >= 1)
    }

    @Test
    fun levelProgressFractionWithinRange() {
        val fraction = GamificationEngine.levelProgressFraction(450)
        assertTrue(fraction in 0f..1f)
        assertEquals(2, GamificationEngine.xpToLevel(450))
    }
}
