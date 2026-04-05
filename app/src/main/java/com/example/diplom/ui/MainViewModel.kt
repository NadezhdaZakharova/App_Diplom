package com.example.diplom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diplom.domain.GamificationEngine
import com.example.diplom.domain.model.Achievement
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.DailyStats
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.PlayerProfile
import com.example.diplom.domain.model.StoryChapter
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.domain.model.WeeklyChallenge
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.domain.repository.ActivityRepository
import com.example.diplom.domain.repository.GamificationRepository
import com.example.diplom.domain.repository.TrainingRepository
import com.example.diplom.domain.usecase.AddStepsUseCase
import com.example.diplom.domain.usecase.BootstrapGameUseCase
import com.example.diplom.domain.usecase.SetDailyGoalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiplomAppNavigationState(
    val showModePicker: Boolean = true,
    val currentDestination: AppDestinations = AppDestinations.TRAINING,
    val sessionActive: Boolean = false,
    val sessionItems: List<WorkoutExercise> = emptyList(),
    val sessionTitle: String = "Тренировка",
    val sessionInstanceId: Int = 0,
    val sessionFromTrainer: Boolean = false
)

data class MainUiState(
    val today: DailyStats = DailyStats("", 0, 0, 0.0),
    val recentDays: List<DailyStats> = emptyList(),
    val dailyGoal: Int = 8000,
    val profile: PlayerProfile = PlayerProfile(0, 1, 0, 0),
    val weeklyChallenge: WeeklyChallenge = WeeklyChallenge("", 55000, 0, false),
    val achievements: List<Achievement> = emptyList(),
    val studentRewards: StudentRewardsStats = StudentRewardsStats(),
    val chapters: List<StoryChapter> = emptyList(),
    val userMode: AppUserMode = AppUserMode.STUDENT,
    val exerciseBank: List<Exercise> = emptyList(),
    val selfWorkout: List<WorkoutExercise> = emptyList(),
    val trainerWorkout: List<WorkoutExercise> = emptyList(),
    val exportedJson: String = "",
    val importStatus: String? = null
) {
    val goalProgressFraction: Float
        get() = (today.steps / dailyGoal.toFloat()).coerceIn(0f, 1f)

    val levelProgressFraction: Float
        get() = GamificationEngine.levelProgressFraction(profile.xp)
}

class MainViewModel(
    activityRepository: ActivityRepository,
    gamificationRepository: GamificationRepository,
    private val trainingRepository: TrainingRepository,
    private val addStepsUseCase: AddStepsUseCase,
    private val setDailyGoalUseCase: SetDailyGoalUseCase,
    private val bootstrapGameUseCase: BootstrapGameUseCase
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
        gamificationRepository.observeAchievements(),
        gamificationRepository.observeChapters()
    ) { weekly, achievements, chapters ->
        GameState(weekly, achievements, chapters)
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
            chapters = game.chapters,
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

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            setDailyGoalUseCase(goal)
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
            val result = trainingRepository.importTrainerWorkoutFromJson(json)
            val message = if (result.isSuccess) {
                "Тренировка от тренера загружена"
            } else {
                "Ошибка загрузки: ${result.exceptionOrNull()?.message ?: "неизвестно"}"
            }
            transferState.update { it.copy(importStatus = message) }
        }
    }

    companion object {
        fun factory(
            activityRepository: ActivityRepository,
            gamificationRepository: GamificationRepository,
            trainingRepository: TrainingRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    activityRepository = activityRepository,
                    gamificationRepository = gamificationRepository,
                    trainingRepository = trainingRepository,
                    addStepsUseCase = AddStepsUseCase(activityRepository, gamificationRepository),
                    setDailyGoalUseCase = SetDailyGoalUseCase(activityRepository, gamificationRepository),
                    bootstrapGameUseCase = BootstrapGameUseCase(gamificationRepository)
                ) as T
            }
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
    val achievements: List<Achievement>,
    val chapters: List<StoryChapter>
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
