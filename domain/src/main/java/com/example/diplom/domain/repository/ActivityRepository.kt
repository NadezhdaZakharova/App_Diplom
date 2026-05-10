package com.example.diplom.domain.repository

import com.example.diplom.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeToday(): Flow<DailyStats>
    fun observeRecentDays(limit: Int = 7): Flow<List<DailyStats>>
    fun observeDailyGoal(): Flow<Int>
    suspend fun addSteps(steps: Int)
    suspend fun setDailyGoal(steps: Int)
}
