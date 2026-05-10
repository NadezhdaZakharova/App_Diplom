package com.example.diplom.data.repository

import com.example.diplom.core.DateUtils
import com.example.diplom.data.local.DailyActivityEntity
import com.example.diplom.data.local.DiplomDao
import com.example.diplom.data.local.UserSettingsEntity
import com.example.diplom.domain.DEFAULT_DAILY_GOAL_STEPS
import com.example.diplom.domain.GamificationEngine
import com.example.diplom.domain.StepMilestoneNotifier
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.repository.ActivityRepository
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val dao: DiplomDao,
    private val stepMilestoneNotifier: StepMilestoneNotifier
) : ActivityRepository {
    override fun observeToday(): Flow<DailyStats> = combine(
        dao.observeRecentActivity(400),
        calendarDayPulse()
    ) { rows, _ ->
        val todayIso = DateUtils.todayIso()
        rows.firstOrNull { it.dateIso == todayIso }?.toDailyStats() ?: DailyStats(
            dateIso = todayIso,
            steps = 0,
            activeMinutes = 0,
            distanceKm = 0.0
        )
    }

    override fun observeRecentDays(limit: Int): Flow<List<DailyStats>> =
        dao.observeRecentActivity(limit).map { list ->
            list.map { it.toDailyStats() }
        }

    override fun observeDailyGoal(): Flow<Int> =
        dao.observeSettings().map { it?.dailyGoal ?: DEFAULT_DAILY_GOAL_STEPS }

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
        val goal = dao.getSettings()?.dailyGoal ?: DEFAULT_DAILY_GOAL_STEPS
        stepMilestoneNotifier.onStepTotalsUpdated(newSteps, goal, todayIso)
    }

    override suspend fun setDailyGoal(steps: Int) {
        val safeGoal = steps.coerceIn(2000, 30000)
        val current = dao.getSettings() ?: UserSettingsEntity()
        dao.upsertSettings(current.copy(dailyGoal = safeGoal))
        val todayIso = DateUtils.todayIso()
        val todaySteps = dao.getDailyActivity(todayIso)?.steps ?: 0
        stepMilestoneNotifier.onStepTotalsUpdated(todaySteps, safeGoal, todayIso)
    }
}

private fun DailyActivityEntity.toDailyStats(): DailyStats = DailyStats(
    dateIso = dateIso,
    steps = steps,
    activeMinutes = activeMinutes,
    distanceKm = GamificationEngine.toDistanceKm(steps)
)

/** Эмиссия при смене календарного дня, чтобы «сегодня» и UI (баннер цели) обновлялись без записи в БД. */
private fun calendarDayPulse(): Flow<Unit> = flow {
    while (true) {
        emit(Unit)
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        val ms = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1000L)
        delay(ms)
    }
}
