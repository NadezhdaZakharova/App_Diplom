package com.example.diplom.data.repository

import com.example.diplom.core.DateUtils
import com.example.diplom.data.local.DailyActivityEntity
import com.example.diplom.data.local.DiplomDao
import com.example.diplom.data.local.UserSettingsEntity
import com.example.diplom.domain.GamificationEngine
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val dao: DiplomDao
) : ActivityRepository {
    override fun observeToday(): Flow<DailyStats> {
        val todayIso = DateUtils.todayIso()
        return dao.observeDailyActivity(todayIso).map { entity ->
            entity?.toDailyStats() ?: DailyStats(
                dateIso = todayIso,
                steps = 0,
                activeMinutes = 0,
                distanceKm = 0.0
            )
        }
    }

    override fun observeRecentDays(limit: Int): Flow<List<DailyStats>> =
        dao.observeRecentActivity(limit).map { list ->
            list.map { it.toDailyStats() }
        }

    override fun observeDailyGoal(): Flow<Int> = dao.observeSettings().map { it?.dailyGoal ?: 8000 }

    override suspend fun addSteps(steps: Int) {
        val todayIso = DateUtils.todayIso()
        val existing = dao.getDailyActivity(todayIso)
        val newSteps = (existing?.steps ?: 0) + steps
        val activeMinutes = (newSteps / 100).coerceAtLeast(0)
        dao.upsertDailyActivity(
            DailyActivityEntity(
                dateIso = todayIso,
                steps = newSteps,
                activeMinutes = activeMinutes
            )
        )
    }

    override suspend fun setDailyGoal(steps: Int) {
        val safeGoal = steps.coerceIn(2000, 30000)
        val current = dao.getSettings() ?: UserSettingsEntity()
        dao.upsertSettings(current.copy(dailyGoal = safeGoal))
    }
}

private fun DailyActivityEntity.toDailyStats(): DailyStats = DailyStats(
    dateIso = dateIso,
    steps = steps,
    activeMinutes = activeMinutes,
    distanceKm = GamificationEngine.toDistanceKm(steps)
)
