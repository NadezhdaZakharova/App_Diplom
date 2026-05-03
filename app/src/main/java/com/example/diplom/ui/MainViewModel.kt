package com.example.diplom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.WeeklyChallenge
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.domain.repository.ActivityRepository
import com.example.diplom.domain.repository.GamificationRepository
import com.example.diplom.domain.repository.TrainingRepository
import com.example.diplom.domain.usecase.AddStepsUseCase
import com.example.diplom.domain.usecase.BootstrapGameUseCase
import com.example.diplom.domain.usecase.ImportWorkoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    activityRepository: ActivityRepository,
    gamificationRepository: GamificationRepository,
    private val trainingRepository: TrainingRepository,
    private val addStepsUseCase: AddStepsUseCase,
    private val bootstrapGameUseCase: BootstrapGameUseCase,
    private val importWorkoutUseCase: ImportWorkoutUseCase
) : ViewModel() {
    private val transferState = MutableStateFlow(TransferState())
    private val _appNavigation = MutableStateFlow(DiplomAppNavigationState())
    val appNavigation: StateFlow<DiplomAppNavigationState> = _appNavigation.asStateFlow()

    private val activityState = combine(
        activityRepository.observeToday(),
        activityRepository.observeRecentDays(),
        activityRepository.observeDailyGoal(),
        gamificationRepository.observeProfile()
    ) { today, recent, goal, profile ->
        ActivityState(today, recent, goal, profile)
    }

    private val gameState = combine(
        gamificationRepository.observeWeeklyChallenge(),
        gamificationRepository.observeAchievements()
    ) { weekly, achievements ->
        GameState(weekly, achievements)
    }

    private val trainingState = combine(
        trainingRepository.observeUserMode(),
        trainingRepository.observeExerciseBank(),
        trainingRepository.observeSelfWorkoutToday(),
        trainingRepository.observeTrainerWorkoutToday()
    ) { mode, bank, selfWorkout, trainerWorkout ->
        TrainingState(mode, bank, selfWorkout, trainerWorkout)
    }

    val uiState: StateFlow<MainUiState> = combine(
        activityState,
        gameState,
        trainingState,
        transferState,
        trainingRepository.observeStudentRewardsStats()
    ) { activity, game, training, transfer, studentRewards ->
        MainUiState(
            today = activity.today,
            recentDays = activity.recent,
            dailyGoal = activity.goal,
            profile = activity.profile,
            weeklyChallenge = game.weekly,
            achievements = game.achievements,
            studentRewards = studentRewards,
            userMode = training.mode,
            exerciseBank = training.bank,
            selfWorkout = training.selfWorkout,
            trainerWorkout = training.trainerWorkout,
            exportedJson = transfer.exportedJson,
            importStatus = transfer.importStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            bootstrapGameUseCase()
            trainingRepository.ensureFirstInstallDateRecorded()
            trainingRepository.seedExerciseBankIfEmpty()
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            addStepsUseCase(steps)
        }
    }

    fun setUserMode(mode: AppUserMode) {
        viewModelScope.launch {
            trainingRepository.setUserMode(mode)
        }
    }

    fun dismissModePicker() {
        _appNavigation.update { it.copy(showModePicker = false) }
    }

    fun setMainDestination(destination: AppDestinations) {
        _appNavigation.update { it.copy(currentDestination = destination) }
    }

    fun syncMainDestinationWithUserMode(mode: AppUserMode) {
        _appNavigation.update { nav ->
            if (mode == AppUserMode.TRAINER && nav.currentDestination == AppDestinations.REWARDS) {
                nav.copy(currentDestination = AppDestinations.TRAINING)
            } else {
                nav
            }
        }
    }

    fun startSelfWorkoutSession(items: List<WorkoutExercise>) {
        _appNavigation.update { nav ->
            val nextId = nav.sessionInstanceId + 1
            nav.copy(
                sessionActive = true,
                sessionItems = items,
                sessionTitle = "Самостоятельная тренировка",
                sessionFromTrainer = false,
                sessionInstanceId = nextId
            )
        }
    }

    fun startTrainerWorkoutSession(items: List<WorkoutExercise>) {
        _appNavigation.update { nav ->
            val nextId = nav.sessionInstanceId + 1
            nav.copy(
                sessionActive = true,
                sessionItems = items,
                sessionTitle = "Тренировка от тренера",
                sessionFromTrainer = true,
                sessionInstanceId = nextId
            )
        }
    }

    fun endWorkoutSession() {
        _appNavigation.update { it.copy(sessionActive = false) }
    }

    fun addExercise(title: String, description: String, reps: Int) {
        viewModelScope.launch {
            trainingRepository.addExercise(title, description, reps)
        }
    }

    fun updateExercise(id: Long, title: String, description: String, reps: Int) {
        viewModelScope.launch {
            trainingRepository.updateExercise(id, title, description, reps)
        }
    }

    fun addToWorkout(exercise: Exercise) {
        viewModelScope.launch {
            trainingRepository.addExerciseToSelfWorkout(exercise)
        }
    }

    fun addToTrainerWorkout(exercise: Exercise) {
        viewModelScope.launch {
            trainingRepository.addExerciseToTrainerWorkout(exercise)
        }
    }

    fun removeWorkoutItem(id: Long) {
        viewModelScope.launch {
            trainingRepository.removeWorkoutItem(id)
        }
    }

    fun moveWorkoutItem(id: Long, moveDown: Boolean) {
        viewModelScope.launch {
            trainingRepository.moveWorkoutItem(id, moveDown)
        }
    }

    fun recordStudentWorkoutCompletion(items: List<WorkoutExercise>, fromTrainerPlan: Boolean) {
        viewModelScope.launch {
            trainingRepository.recordCompletedStudentWorkout(items, fromTrainerPlan)
        }
    }

    fun exportTrainerWorkout() {
        viewModelScope.launch {
            val json = trainingRepository.exportTrainerWorkoutAsJson()
            transferState.update {
                it.copy(
                    exportedJson = json,
                    importStatus = "Файл тренировки для ученика готов — скопируйте JSON ниже и отправьте"
                )
            }
        }
    }

    fun importTrainerWorkout(json: String) {
        viewModelScope.launch {
            val result = importWorkoutUseCase(json)
            val message = if (result.isSuccess) {
                "Тренировка от тренера загружена"
            } else {
                "Ошибка загрузки: ${result.exceptionOrNull()?.message ?: "неизвестно"}"
            }
            transferState.update { it.copy(importStatus = message) }
        }
    }

    /**
     * Входящий текст из меню «Поделиться» (ACTION_SEND).
     * Валидирует JSON, импортирует план и открывает раздел ученика с экраном тренировки.
     */
    fun importWorkoutFromShareIntent(rawText: String) {
        viewModelScope.launch {
            val result = importWorkoutUseCase(rawText)
            val message = if (result.isSuccess) {
                trainingRepository.setUserMode(AppUserMode.STUDENT)
                _appNavigation.update {
                    it.copy(
                        showModePicker = false,
                        currentDestination = AppDestinations.TRAINING,
                        sessionActive = false
                    )
                }
                "Тренировка из «Поделиться» загружена — откройте «От тренера»"
            } else {
                "Не удалось импортировать: ${result.exceptionOrNull()?.message ?: "неизвестно"}"
            }
            transferState.update { it.copy(importStatus = message) }
        }
    }

}

private data class ActivityState(
    val today: DailyStats,
    val recent: List<DailyStats>,
    val goal: Int,
    val profile: PlayerProfile
)

private data class GameState(
    val weekly: WeeklyChallenge,
    val achievements: List<Achievement>
)

private data class TrainingState(
    val mode: AppUserMode,
    val bank: List<Exercise>,
    val selfWorkout: List<WorkoutExercise>,
    val trainerWorkout: List<WorkoutExercise>
)

private data class TransferState(
    val exportedJson: String = "",
    val importStatus: String? = null
)
