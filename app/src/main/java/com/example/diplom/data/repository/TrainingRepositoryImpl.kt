package com.example.diplom.data.repository

import com.example.diplom.core.DateUtils
import com.example.diplom.data.local.CompletedWorkoutSessionEntity
import com.example.diplom.data.local.DiplomDao
import com.example.diplom.data.local.ExerciseCompletionEventEntity
import com.example.diplom.data.local.UserSettingsEntity
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.domain.repository.TrainingRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
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

    override suspend fun updateExercise(id: Long, title: String, description: String, defaultReps: Int) {
        if (id <= 0L) return
        val safeTitle = title.trim()
        if (safeTitle.isEmpty()) return
        dao.upsertExercise(
            com.example.diplom.data.local.ExerciseEntity(
                id = id,
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
                    description = it.description,
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
                    description = it.description,
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
                description = exercise.description,
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
                description = exercise.description,
                plannedReps = exercise.defaultReps,
                sortOrder = nextOrder,
                workoutType = WORKOUT_TRAINER
            )
        )
    }

    override suspend fun removeWorkoutItem(id: Long) {
        dao.deletePlannedWorkoutItem(id)
    }

    override suspend fun moveWorkoutItem(id: Long, moveDown: Boolean) {
        val all = dao.getPlannedWorkoutAll()
        val current = all.firstOrNull { it.id == id } ?: return
        val scoped = all
            .filter { it.dateIso == current.dateIso && it.workoutType == current.workoutType }
            .sortedWith(compareBy<com.example.diplom.data.local.PlannedWorkoutEntity> { it.sortOrder }.thenBy { it.id })
        val currentIndex = scoped.indexOfFirst { it.id == id }
        if (currentIndex == -1) return
        val swapIndex = if (moveDown) currentIndex + 1 else currentIndex - 1
        if (swapIndex !in scoped.indices) return
        val swapWith = scoped[swapIndex]
        dao.upsertPlannedWorkouts(
            listOf(
                current.copy(sortOrder = swapWith.sortOrder),
                swapWith.copy(sortOrder = current.sortOrder)
            )
        )
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
                description = item.optString("description", ""),
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
                                .put("description", it.description)
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
