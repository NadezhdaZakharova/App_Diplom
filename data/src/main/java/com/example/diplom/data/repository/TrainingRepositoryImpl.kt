package com.example.diplom.data.repository

import com.example.diplom.core.DateUtils
import com.example.diplom.data.local.CompletedWorkoutSessionEntity
import com.example.diplom.data.local.DiplomDao
import com.example.diplom.data.local.ExerciseCompletionEventEntity
import com.example.diplom.data.local.PlannedWorkoutEntity
import com.example.diplom.data.local.UserSettingsEntity
import com.example.diplom.domain.DEFAULT_DAILY_GOAL_STEPS
import com.example.diplom.domain.ExerciseTitleKeys
import com.example.diplom.domain.StudentRewardsCalculator
import com.example.diplom.domain.TrainingConstraints
import com.example.diplom.domain.stepsMeetWorkoutStreakAlternative
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.ExerciseCompletionSnapshot
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.domain.model.WorkoutSessionSnapshot
import com.example.diplom.domain.repository.TrainingRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val dao: DiplomDao,
    private val trainerWorkoutJsonStore: TrainerWorkoutJsonStore
) : TrainingRepository {
    private companion object {
        const val WORKOUT_SELF = "SELF"
        const val WORKOUT_TRAINER = "TRAINER"
        const val WORKOUT_STEPS = "STEPS"
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
            items.map { Exercise(it.id, it.title, it.description, it.defaultReps, it.titleKey) }
        }

    override suspend fun seedExerciseBankIfEmpty() {
        if (dao.getExercises().isNotEmpty()) return
        dao.upsertExercises(
            listOf(
                com.example.diplom.data.local.ExerciseEntity(
                    title = "Приседания",
                    description = "Базовое упражнение на ноги и корпус",
                    defaultReps = 15,
                    titleKey = ExerciseTitleKeys.PRESET_SQUATS
                ),
                com.example.diplom.data.local.ExerciseEntity(
                    title = "Отжимания",
                    description = "Классические отжимания от пола",
                    defaultReps = 10,
                    titleKey = ExerciseTitleKeys.PRESET_PUSHUPS
                ),
                com.example.diplom.data.local.ExerciseEntity(
                    title = "Планка",
                    description = "Удержание позиции в секундах",
                    defaultReps = 45,
                    titleKey = ExerciseTitleKeys.PRESET_PLANK
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
                defaultReps = defaultReps.coerceIn(1, TrainingConstraints.MAX_EXERCISE_DURATION_SECONDS)
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
                defaultReps = defaultReps.coerceIn(1, TrainingConstraints.MAX_EXERCISE_DURATION_SECONDS),
                titleKey = null
            )
        )
    }

    override fun observeSelfWorkoutToday(): Flow<List<WorkoutExercise>> =
        observePlannedWorkoutToday(WORKOUT_SELF)

    override fun observeTrainerWorkoutToday(): Flow<List<WorkoutExercise>> =
        observePlannedWorkoutToday(WORKOUT_TRAINER)

    override suspend fun addExerciseToSelfWorkout(exercise: Exercise) {
        addExerciseToPlannedWorkout(exercise, WORKOUT_SELF)
    }

    override suspend fun addExerciseToTrainerWorkout(exercise: Exercise) {
        addExerciseToPlannedWorkout(exercise, WORKOUT_TRAINER)
    }

    override suspend fun removeWorkoutItem(id: Long) {
        val all = dao.getPlannedWorkoutAll()
        val target = all.firstOrNull { it.id == id } ?: return
        dao.deletePlannedWorkoutItem(id)
        renumberPlannedWorkoutSortOrders(target.dateIso, target.workoutType)
    }

    override suspend fun moveWorkoutItem(id: Long, moveDown: Boolean) {
        val all = dao.getPlannedWorkoutAll()
        val current = all.firstOrNull { it.id == id } ?: return
        val scoped = all
            .filter { it.dateIso == current.dateIso && it.workoutType == current.workoutType }
            .sortedWith(compareBy<PlannedWorkoutEntity> { it.sortOrder }.thenBy { it.id })
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
        renumberPlannedWorkoutSortOrders(current.dateIso, current.workoutType)
    }

    private suspend fun renumberPlannedWorkoutSortOrders(dateIso: String, workoutType: String) {
        val ordered = dao.getPlannedWorkoutAll()
            .filter { it.dateIso == dateIso && it.workoutType == workoutType }
            .sortedWith(compareBy<PlannedWorkoutEntity> { it.sortOrder }.thenBy { it.id })
        val renumbered = ordered.mapIndexed { idx, entity -> entity.copy(sortOrder = idx) }
        if (renumbered.isNotEmpty()) {
            dao.upsertPlannedWorkouts(renumbered)
        }
    }

    override suspend fun recordCompletedStudentWorkout(items: List<WorkoutExercise>, fromTrainerPlan: Boolean) {
        if (items.isEmpty()) return
        val dateIso = DateUtils.todayIso()
        val kind = if (fromTrainerPlan) WORKOUT_TRAINER else WORKOUT_SELF
        dao.insertCompletedWorkoutSession(
            CompletedWorkoutSessionEntity(dateIso = dateIso, workoutKind = kind)
        )
        val bankById = dao.getExercises().associateBy { it.id }
        dao.insertExerciseCompletionEvents(
            items.map { item ->
                val titleKey = item.titleKey ?: bankById[item.exerciseId]?.titleKey
                ExerciseCompletionEventEntity(
                    dateIso = dateIso,
                    exerciseId = item.exerciseId,
                    title = item.title,
                    titleKey = titleKey
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
        dao.observeSettings(),
        dao.observeRecentActivity(400)
    ) { events, sessions, settings, activity ->
        val today = LocalDate.now()
        val weekStart = LocalDate.parse(DateUtils.isoWeekStart(today))
        val stepsByDate = activity.associate { it.dateIso to it.steps }
        val goal = settings?.dailyGoal ?: DEFAULT_DAILY_GOAL_STEPS
        StudentRewardsCalculator.buildStats(
            events = events.map {
                ExerciseCompletionSnapshot(
                    dateIso = it.dateIso,
                    exerciseId = it.exerciseId,
                    title = it.title,
                    titleKey = it.titleKey
                )
            },
            sessions = sessions.map {
                WorkoutSessionSnapshot(dateIso = it.dateIso, workoutKind = it.workoutKind)
            },
            firstInstallDateIso = settings?.firstInstallDateIso.orEmpty(),
            today = today,
            calendarWeekStart = weekStart,
            stepsByDateIso = stepsByDate,
            dailyGoal = goal
        )
    }

    override fun observeStepsConversionPromptVisible(): Flow<Boolean> = combine(
        dao.observeRecentActivity(14),
        dao.observeSettings(),
        dao.observeCompletedWorkoutSessions()
    ) { activity, settings, sessions ->
        val todayIso = DateUtils.todayIso()
        val steps = activity.firstOrNull { it.dateIso == todayIso }?.steps ?: 0
        val goal = settings?.dailyGoal ?: DEFAULT_DAILY_GOAL_STEPS
        val threshold = stepsMeetWorkoutStreakAlternative(steps, goal)
        val declinedToday = settings?.stepsToWorkoutOfferDeclinedDateIso == todayIso
        val hasSessionToday = sessions.any { it.dateIso == todayIso }
        threshold && !declinedToday && !hasSessionToday
    }

    override suspend fun declineStepsToWorkoutConversion() {
        val todayIso = DateUtils.todayIso()
        val current = dao.getSettings() ?: UserSettingsEntity()
        dao.upsertSettings(current.copy(stepsToWorkoutOfferDeclinedDateIso = todayIso))
    }

    override suspend fun recordWorkoutFromStepsConversion() {
        dao.insertCompletedWorkoutSession(
            CompletedWorkoutSessionEntity(dateIso = DateUtils.todayIso(), workoutKind = WORKOUT_STEPS)
        )
    }

    override suspend fun importTrainerWorkoutFromJson(json: String): Result<Unit> = runCatching {
        val todayIso = DateUtils.todayIso()
        val normalized = trainerWorkoutJsonStore.parseTrainerWorkoutEntities(json, todayIso)
        dao.clearPlannedWorkoutByTypeAndDate(todayIso, WORKOUT_TRAINER)
        dao.upsertPlannedWorkouts(normalized)
    }

    override suspend fun exportTrainerWorkoutAsJson(): String {
        val todayIso = DateUtils.todayIso()
        val rows = dao.getPlannedWorkoutAll()
            .filter { it.workoutType == WORKOUT_TRAINER && it.dateIso == todayIso }
            .sortedBy { it.sortOrder }
        return trainerWorkoutJsonStore.buildExportJson(rows)
    }

    private fun observePlannedWorkoutToday(workoutType: String): Flow<List<WorkoutExercise>> {
        val todayIso = DateUtils.todayIso()
        return dao.observePlannedWorkout(todayIso, workoutType).map { items ->
            items.map { it.toWorkoutExercise() }
        }
    }

    private suspend fun addExerciseToPlannedWorkout(exercise: Exercise, workoutType: String) {
        val todayIso = DateUtils.todayIso()
        val nextOrder = dao.getPlannedWorkoutAll()
            .filter { it.dateIso == todayIso && it.workoutType == workoutType }
            .maxOfOrNull { it.sortOrder + 1 } ?: 0
        dao.upsertPlannedWorkout(
            PlannedWorkoutEntity(
                dateIso = todayIso,
                exerciseId = exercise.id,
                title = exercise.title,
                description = exercise.description,
                plannedReps = exercise.defaultReps,
                sortOrder = nextOrder,
                workoutType = workoutType,
                titleKey = exercise.titleKey
            )
        )
    }
}

private fun PlannedWorkoutEntity.toWorkoutExercise(): WorkoutExercise = WorkoutExercise(
    id = id,
    dateIso = dateIso,
    exerciseId = exerciseId,
    title = title,
    description = description,
    plannedReps = plannedReps,
    sortOrder = sortOrder,
    titleKey = titleKey
)
