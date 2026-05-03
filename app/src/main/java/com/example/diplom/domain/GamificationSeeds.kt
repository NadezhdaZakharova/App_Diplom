package com.example.diplom.domain

import com.example.diplom.data.local.AchievementEntity

internal fun initialAchievementsData(): List<AchievementEntity> = listOf(
    AchievementEntity("first_steps", "First Steps", "Reach 1,000 steps in a day", false, null),
    AchievementEntity("goal_crusher", "Goal Crusher", "Complete daily goal 3 days", false, null),
    AchievementEntity("trail_runner", "Trail Runner", "Reach 50,000 total steps", false, null),
    AchievementEntity("streak_7", "Streak Keeper", "Maintain a 7-day streak", false, null)
)
