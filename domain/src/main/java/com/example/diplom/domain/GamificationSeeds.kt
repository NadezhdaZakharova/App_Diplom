package com.example.diplom.domain

import com.example.diplom.domain.model.Achievement

internal fun initialAchievementsData(): List<Achievement> = listOf(
    Achievement("first_steps", "First Steps", "Reach 1,000 steps in a day", false, null),
    Achievement(
        "goal_crusher",
        "Goal Crusher",
        "Meet 1.5× your daily step goal on 3 days",
        false,
        null
    ),
    Achievement("trail_runner", "Trail Runner", "Reach 50,000 total steps", false, null),
    Achievement("streak_7", "Streak Keeper", "Maintain a 7-day step streak (1.5× goal)", false, null)
)
