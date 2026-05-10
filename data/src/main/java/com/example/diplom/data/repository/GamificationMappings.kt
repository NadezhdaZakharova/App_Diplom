package com.example.diplom.data.repository

import com.example.diplom.data.local.AchievementEntity
import com.example.diplom.data.local.DailyActivityEntity
import com.example.diplom.data.local.WeeklyChallengeEntity
import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.DailyStepRecord
import com.example.diplom.domain.model.WeeklyChallenge

internal fun DailyActivityEntity.toStepRecord(): DailyStepRecord =
    DailyStepRecord(dateIso = dateIso, steps = steps, activeMinutes = activeMinutes)

internal fun AchievementEntity.toAchievement(): Achievement =
    Achievement(id = id, title = title, description = description, unlocked = unlocked, unlockedAtIso = unlockedAtIso)

internal fun Achievement.toEntity(): AchievementEntity =
    AchievementEntity(id = id, title = title, description = description, unlocked = unlocked, unlockedAtIso = unlockedAtIso)

internal fun WeeklyChallengeEntity.toWeeklyChallenge(): WeeklyChallenge =
    WeeklyChallenge(
        weekStartIso = weekStartIso,
        targetSteps = targetSteps,
        progressSteps = progressSteps,
        completed = completed
    )

internal fun WeeklyChallenge.toEntity(rowId: Int = 0): WeeklyChallengeEntity =
    WeeklyChallengeEntity(
        id = rowId,
        weekStartIso = weekStartIso,
        targetSteps = targetSteps,
        progressSteps = progressSteps,
        completed = completed
    )
