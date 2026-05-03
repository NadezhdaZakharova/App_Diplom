package com.example.diplom.domain.repository

import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.WeeklyChallenge
import kotlinx.coroutines.flow.Flow

interface GamificationRepository {
    fun observeProfile(): Flow<PlayerProfile>
    fun observeAchievements(): Flow<List<Achievement>>
    fun observeWeeklyChallenge(): Flow<WeeklyChallenge>
    suspend fun seedIfEmpty()
    suspend fun recalculate()
}
