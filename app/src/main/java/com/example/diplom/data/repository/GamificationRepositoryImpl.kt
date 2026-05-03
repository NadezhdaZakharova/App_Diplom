package com.example.diplom.data.repository

import com.example.diplom.core.DateUtils
import com.example.diplom.data.local.DiplomDao
import com.example.diplom.data.local.UserSettingsEntity
import com.example.diplom.domain.GamificationEngine
import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.WeeklyChallenge
import com.example.diplom.domain.repository.GamificationRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepositoryImpl @Inject constructor(
    private val dao: DiplomDao
) : GamificationRepository {
    override fun observeProfile(): Flow<PlayerProfile> =
        combine(dao.observeSettings(), dao.observeRecentActivity(365)) { settings, activity ->
            val dailyGoal = settings?.dailyGoal ?: 8000
            val progress = GamificationEngine.calculatePlayerProgress(activity, dailyGoal)
            PlayerProfile(
                xp = progress.xp,
                level = progress.level,
                streakDays = progress.streakDays,
                bestStreakDays = progress.bestStreakDays
            )
        }

    override fun observeAchievements(): Flow<List<Achievement>> =
        dao.observeAchievements().map { list ->
            list.map {
                Achievement(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    unlocked = it.unlocked,
                    unlockedAtIso = it.unlockedAtIso
                )
            }
        }

    override fun observeWeeklyChallenge(): Flow<WeeklyChallenge> =
        dao.observeWeeklyChallenge().map { item ->
            val fallback = item ?: GamificationEngine.buildWeeklyChallenge(
                existing = null,
                weekStartIso = DateUtils.isoWeekStart(),
                weekSteps = 0
            )
            WeeklyChallenge(
                weekStartIso = fallback.weekStartIso,
                targetSteps = fallback.targetSteps,
                progressSteps = fallback.progressSteps,
                completed = fallback.completed
            )
        }

    override suspend fun seedIfEmpty() {
        if (dao.getSettings() == null) {
            dao.upsertSettings(UserSettingsEntity(firstInstallDateIso = DateUtils.todayIso()))
        }
        if (dao.getAchievements().isEmpty()) {
            dao.upsertAchievements(GamificationEngine.initialAchievements())
        }
        if (dao.getWeeklyChallenge() == null) {
            dao.upsertWeeklyChallenge(
                GamificationEngine.buildWeeklyChallenge(
                    existing = null,
                    weekStartIso = DateUtils.isoWeekStart(),
                    weekSteps = 0
                )
            )
        }
    }

    override suspend fun recalculate() {
        val allActivity = dao.getAllActivity()
        val settings = dao.getSettings() ?: UserSettingsEntity()
        val dailyGoal = settings.dailyGoal
        val todayIso = DateUtils.todayIso()
        val achievements = dao.getAchievements()
        val updatedAchievements = GamificationEngine.updateAchievements(
            existing = achievements,
            activity = allActivity,
            dailyGoal = dailyGoal,
            todayIso = todayIso
        )
        dao.upsertAchievements(updatedAchievements)

        val weekStart = DateUtils.isoWeekStart()
        val weekStartDate = LocalDate.parse(weekStart)
        val weekSteps = allActivity.filter { LocalDate.parse(it.dateIso) >= weekStartDate }.sumOf { it.steps }
        val weekly = GamificationEngine.buildWeeklyChallenge(
            existing = dao.getWeeklyChallenge(),
            weekStartIso = weekStart,
            weekSteps = weekSteps
        )
        dao.upsertWeeklyChallenge(weekly)
    }
}
