package com.example.diplom.data.repository

import com.example.diplom.core.DateUtils
import com.example.diplom.data.local.DailyActivityEntity
import com.example.diplom.data.local.DiplomDao
import com.example.diplom.data.local.UserSettingsEntity
import com.example.diplom.domain.GamificationEngine
import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.StoryChapter
import com.example.diplom.domain.model.WeeklyChallenge
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.domain.repository.ActivityRepository
import com.example.diplom.domain.repository.GamificationRepository
import com.example.diplom.domain.repository.LeaderboardRepository
import com.example.diplom.domain.repository.SyncRepository
import com.example.diplom.domain.repository.TrainingRepository
import java.time.LocalDate
import com.example.diplom.data.local.CompletedWorkoutSessionEntity
import com.example.diplom.data.local.ExerciseCompletionEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ActivityRepositoryImpl(
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

class GamificationRepositoryImpl(
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

    override fun observeChapters(): Flow<List<StoryChapter>> =
        dao.observeChapters().map { list ->
            list.map {
                StoryChapter(
                    chapterNumber = it.chapterNumber,
                    title = it.title,
                    requiredDistanceKm = it.requiredDistanceKm,
                    questSteps = it.questSteps,
                    unlocked = it.unlocked
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
        if (dao.getChapters().isEmpty()) {
            dao.upsertChapters(GamificationEngine.initialChapters())
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
        val todaySteps = allActivity.firstOrNull { it.dateIso == todayIso }?.steps ?: 0
        val totalDistance = GamificationEngine.toDistanceKm(allActivity.sumOf { it.steps })
        val chapters = dao.getChapters()
        val updatedChapters = GamificationEngine.updateChapters(chapters, totalDistance, todaySteps)
        dao.upsertChapters(updatedChapters)

        val achievements = dao.getAchievements()
        val updatedAchievements = GamificationEngine.updateAchievements(
            existing = achievements,
            activity = allActivity,
            dailyGoal = dailyGoal,
            chapters = updatedChapters,
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

class OfflineLeaderboardRepository : LeaderboardRepository {
    override suspend fun topPlayers(): List<String> =
        listOf("You", "RangerBot", "MageWalker", "KnightSteps")
}

class OfflineSyncRepository : SyncRepository {
    override suspend fun syncNow(): Boolean = true
}

class TrainingRepositoryImpl(
    private val dao: DiplomDao
) : TrainingRepository {
    private companion object {
        const val WORKOUT_SELF = "SELF"
        const val WORKOUT_TRAINER = "TRAINER"
    }

    override fun observeUserMode(): Flow<AppUserMode> =
        dao.observeSettings().map { settings ->
            runCatching { AppUserMode.valueOf(settings?.mode ?: AppUserMode.STUDENT.name) }
                .getOrDefault(AppUserMode.STUDENT)
        }

    override suspend fun setUserMode(mode: AppUserMode) {
        val current = dao.getSettings() ?: UserSettingsEntity()
        dao.upsertSettings(current.copy(mode = mode.name))
    }

    override fun observeExerciseBank(): Flow<List<Exercise>> =
        dao.observeExercises().map { items ->
            items.map { Exercise(it.id, it.title, it.description, it.defaultReps) }
        }

    override suspend fun seedExerciseBankIfEmpty() {
        if (dao.getExercises().isNotEmpty()) return
        dao.upsertExercises(
            listOf(
                com.example.diplom.data.local.ExerciseEntity(
                    title = "Приседания",
                    description = "Базовое упражнение на ноги и корпус",
                    defaultReps = 15
                ),
                com.example.diplom.data.local.ExerciseEntity(
                    title = "Отжимания",
                    description = "Классические отжимания от пола",
                    defaultReps = 10
                ),
                com.example.diplom.data.local.ExerciseEntity(
                    title = "Планка",
                    description = "Удержание позиции в секундах",
                    defaultReps = 45
                )
            )
        )
    }

    override suspend fun addExercise(title: String, description: String, defaultReps: Int) {
        val safeTitle = title.trim()
        if (safeTitle.isEmpty()) return
        dao.upsertExercise(
            com.example.diplom.data.local.ExerciseEntity(
                title = safeTitle,
                description = description.trim(),
                defaultReps = defaultReps.coerceIn(1, 500)
            )
        )
    }

    override fun observeSelfWorkoutToday(): Flow<List<WorkoutExercise>> {
        val todayIso = DateUtils.todayIso()
        return dao.observePlannedWorkout(todayIso, WORKOUT_SELF).map { items ->
            items.map {
                WorkoutExercise(
                    id = it.id,
                    dateIso = it.dateIso,
                    exerciseId = it.exerciseId,
                    title = it.title,
                    plannedReps = it.plannedReps,
                    sortOrder = it.sortOrder
                )
            }
        }
    }

    override fun observeTrainerWorkoutToday(): Flow<List<WorkoutExercise>> {
        val todayIso = DateUtils.todayIso()
        return dao.observePlannedWorkout(todayIso, WORKOUT_TRAINER).map { items ->
            items.map {
                WorkoutExercise(
                    id = it.id,
                    dateIso = it.dateIso,
                    exerciseId = it.exerciseId,
                    title = it.title,
                    plannedReps = it.plannedReps,
                    sortOrder = it.sortOrder
                )
            }
        }
    }

    override suspend fun addExerciseToSelfWorkout(exercise: Exercise) {
        val todayIso = DateUtils.todayIso()
        val nextOrder = dao.getPlannedWorkoutAll()
            .filter { it.dateIso == todayIso && it.workoutType == WORKOUT_SELF }
            .maxOfOrNull { it.sortOrder + 1 } ?: 0
        dao.upsertPlannedWorkout(
            com.example.diplom.data.local.PlannedWorkoutEntity(
                dateIso = todayIso,
                exerciseId = exercise.id,
                title = exercise.title,
                plannedReps = exercise.defaultReps,
                sortOrder = nextOrder,
                workoutType = WORKOUT_SELF
            )
        )
    }

    override suspend fun addExerciseToTrainerWorkout(exercise: Exercise) {
        val todayIso = DateUtils.todayIso()
        val nextOrder = dao.getPlannedWorkoutAll()
            .filter { it.dateIso == todayIso && it.workoutType == WORKOUT_TRAINER }
            .maxOfOrNull { it.sortOrder + 1 } ?: 0
        dao.upsertPlannedWorkout(
            com.example.diplom.data.local.PlannedWorkoutEntity(
                dateIso = todayIso,
                exerciseId = exercise.id,
                title = exercise.title,
                plannedReps = exercise.defaultReps,
                sortOrder = nextOrder,
                workoutType = WORKOUT_TRAINER
            )
        )
    }

    override suspend fun removeWorkoutItem(id: Long) {
        dao.deletePlannedWorkoutItem(id)
    }

    override suspend fun recordCompletedStudentWorkout(items: List<WorkoutExercise>, fromTrainerPlan: Boolean) {
        if (items.isEmpty()) return
        val dateIso = DateUtils.todayIso()
        val kind = if (fromTrainerPlan) WORKOUT_TRAINER else WORKOUT_SELF
        dao.insertCompletedWorkoutSession(
            CompletedWorkoutSessionEntity(dateIso = dateIso, workoutKind = kind)
        )
        dao.insertExerciseCompletionEvents(
            items.map { item ->
                ExerciseCompletionEventEntity(
                    dateIso = dateIso,
                    exerciseId = item.exerciseId,
                    title = item.title
                )
            }
        )
    }

    override suspend fun ensureFirstInstallDateRecorded() {
        val current = dao.getSettings() ?: return
        if (current.firstInstallDateIso.isBlank()) {
            dao.upsertSettings(current.copy(firstInstallDateIso = DateUtils.todayIso()))
        }
    }

    override fun observeStudentRewardsStats(): Flow<StudentRewardsStats> = combine(
        dao.observeExerciseCompletionEvents(),
        dao.observeCompletedWorkoutSessions(),
        dao.observeSettings()
    ) { events, sessions, settings ->
        buildStudentRewardsStats(
            events = events,
            sessions = sessions,
            firstInstallDateIso = settings?.firstInstallDateIso.orEmpty()
        )
    }

    override suspend fun importTrainerWorkoutFromJson(json: String): Result<Unit> = runCatching {
        val root = JSONObject(json)
        val todayIso = DateUtils.todayIso()
        val trainerArray = root.optJSONArray("trainerWorkout")
        val plannedArray = root.optJSONArray("plannedWorkout") ?: JSONArray()
        val fromDedicatedTrainerFile = trainerArray != null && trainerArray.length() > 0
        val sourceArray = if (fromDedicatedTrainerFile) trainerArray else plannedArray
        dao.clearPlannedWorkoutByTypeAndDate(todayIso, WORKOUT_TRAINER)
        val trainerItems = mutableListOf<com.example.diplom.data.local.PlannedWorkoutEntity>()
        repeat(sourceArray.length()) { index ->
            val item = sourceArray.getJSONObject(index)
            if (!fromDedicatedTrainerFile) {
                val itemDate = item.optString("dateIso", todayIso)
                if (itemDate != todayIso) return@repeat
                val itemType = item.optString("workoutType", WORKOUT_TRAINER)
                if (itemType != WORKOUT_TRAINER) return@repeat
            }
            trainerItems += com.example.diplom.data.local.PlannedWorkoutEntity(
                dateIso = todayIso,
                exerciseId = item.optLong("exerciseId", 0L),
                title = item.getString("title"),
                plannedReps = item.optInt("plannedReps", 10),
                sortOrder = item.optInt("sortOrder", index),
                workoutType = WORKOUT_TRAINER
            )
        }
        if (trainerItems.isNotEmpty()) {
            dao.upsertPlannedWorkouts(trainerItems)
        }
    }

    override suspend fun exportTrainerWorkoutAsJson(): String {
        val todayIso = DateUtils.todayIso()
        val root = JSONObject()
        root.put("schemaVersion", 1)
        root.put(
            "trainerWorkout",
            JSONArray().apply {
                dao.getPlannedWorkoutAll()
                    .filter { it.workoutType == WORKOUT_TRAINER && it.dateIso == todayIso }
                    .sortedBy { it.sortOrder }
                    .forEach {
                        put(
                            JSONObject()
                                .put("dateIso", it.dateIso)
                                .put("exerciseId", it.exerciseId)
                                .put("title", it.title)
                                .put("plannedReps", it.plannedReps)
                                .put("sortOrder", it.sortOrder)
                                .put("workoutType", it.workoutType)
                        )
                    }
            }
        )
        return root.toString(2)
    }
}

private fun buildStudentRewardsStats(
    events: List<ExerciseCompletionEventEntity>,
    sessions: List<CompletedWorkoutSessionEntity>,
    firstInstallDateIso: String
): StudentRewardsStats {
    val firstWeekTarget = 3
    val varietyTarget = 3
    val today = LocalDate.now()
    val weekStart = LocalDate.parse(DateUtils.isoWeekStart(today))
    val weekEnd = weekStart.plusDays(6)
    val monthStart = today.withDayOfMonth(1)
    val monthEnd = today.withDayOfMonth(today.lengthOfMonth())

    val weekEvents = events.filter { rewardsDateInRange(it.dateIso, weekStart, weekEnd) }
    val topEntry = weekEvents
        .groupingBy { it.exerciseId }
        .eachCount()
        .maxByOrNull { it.value }
    val topTitle = topEntry?.let { entry ->
        weekEvents.firstOrNull { it.exerciseId == entry.key }?.title
    }

    val sessionDatesThisWeek = sessions
        .map { it.dateIso }
        .filter { rewardsDateInRange(it, weekStart, weekEnd) }
        .toSet()
    val marathonComplete = (0L..6L).all { offset ->
        weekStart.plusDays(offset).toString() in sessionDatesThisWeek
    }

    val monthCount = sessions.count { rewardsDateInRange(it.dateIso, monthStart, monthEnd) }

    val allSessionDates = sessions.map { it.dateIso }.toSet()
    val streakDays = computeWorkoutStreakDays(allSessionDates, today)

    val installDate = runCatching {
        LocalDate.parse(firstInstallDateIso.ifBlank { today.toString() })
    }.getOrDefault(today)
    val firstWeekEnd = installDate.plusDays(6)
    val firstWeekCount = sessions.count { s ->
        rewardsDateInRange(s.dateIso, installDate, firstWeekEnd)
    }

    val distinctExercisesWeek = weekEvents.map { it.exerciseId }.distinct().size
    val completedTrainer = sessions.any { it.workoutKind == "TRAINER" }

    return StudentRewardsStats(
        weekTopExerciseTitle = topTitle,
        weekTopExerciseCount = topEntry?.value ?: 0,
        weeklyMarathonComplete = marathonComplete,
        workoutDaysThisWeek = sessionDatesThisWeek.size,
        workoutSessionsThisMonth = monthCount,
        daysInMonth = monthEnd.dayOfMonth,
        currentStreakDays = streakDays,
        streakUnlocked3 = streakDays >= 3,
        streakUnlocked7 = streakDays >= 7,
        streakUnlocked14 = streakDays >= 14,
        streakUnlocked30 = streakDays >= 30,
        firstWeekWorkoutsCount = firstWeekCount,
        firstWeekTarget = firstWeekTarget,
        firstWeekComplete = firstWeekCount >= firstWeekTarget,
        varietyDistinctExercises = distinctExercisesWeek,
        varietyTarget = varietyTarget,
        varietyComplete = distinctExercisesWeek >= varietyTarget,
        completedTrainerWorkout = completedTrainer
    )
}

private fun computeWorkoutStreakDays(sessionDates: Set<String>, today: LocalDate): Int {
    var d = today
    var streak = 0
    while (sessionDates.contains(d.toString())) {
        streak++
        d = d.minusDays(1)
    }
    return streak
}

private fun rewardsDateInRange(dateIso: String, start: LocalDate, endInclusive: LocalDate): Boolean {
    val d = LocalDate.parse(dateIso)
    return !d.isBefore(start) && !d.isAfter(endInclusive)
}

private fun DailyActivityEntity.toDailyStats(): DailyStats = DailyStats(
    dateIso = dateIso,
    steps = steps,
    activeMinutes = activeMinutes,
    distanceKm = GamificationEngine.toDistanceKm(steps)
)
