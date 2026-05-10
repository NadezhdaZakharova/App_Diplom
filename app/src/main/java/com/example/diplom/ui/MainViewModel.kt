package com.example.diplom.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplom.R
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.domain.usecase.AddExerciseToBankUseCase
import com.example.diplom.domain.usecase.AddExerciseToSelfWorkoutUseCase
import com.example.diplom.domain.usecase.AddExerciseToTrainerWorkoutUseCase
import com.example.diplom.domain.usecase.AddStepsUseCase
import com.example.diplom.domain.usecase.ApplyShareImportAsStudentUseCase
import com.example.diplom.domain.usecase.BootstrapGameUseCase
import com.example.diplom.domain.usecase.DeclineStepsToWorkoutConversionUseCase
import com.example.diplom.domain.usecase.ExportTrainerWorkoutAsJsonUseCase
import com.example.diplom.domain.usecase.ImportWorkoutUseCase
import com.example.diplom.domain.usecase.MovePlannedWorkoutItemUseCase
import com.example.diplom.domain.usecase.PrepareTrainingStorageUseCase
import com.example.diplom.domain.usecase.RecordCompletedStudentWorkoutUseCase
import com.example.diplom.domain.usecase.RecordWorkoutFromStepsConversionUseCase
import com.example.diplom.domain.usecase.RemovePlannedWorkoutItemUseCase
import com.example.diplom.domain.usecase.SetDailyGoalUseCase
import com.example.diplom.domain.usecase.SetUserModeUseCase
import com.example.diplom.domain.usecase.UpdateExerciseInBankUseCase
import com.example.diplom.domain.usecase.ObserveCoreMainUiInputsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val observeCoreMainUiInputsUseCase: ObserveCoreMainUiInputsUseCase,
    private val addStepsUseCase: AddStepsUseCase,
    private val bootstrapGameUseCase: BootstrapGameUseCase,
    private val importWorkoutUseCase: ImportWorkoutUseCase,
    private val applyShareImportAsStudentUseCase: ApplyShareImportAsStudentUseCase,
    private val setDailyGoalUseCase: SetDailyGoalUseCase,
    private val prepareTrainingStorageUseCase: PrepareTrainingStorageUseCase,
    private val declineStepsToWorkoutConversionUseCase: DeclineStepsToWorkoutConversionUseCase,
    private val recordWorkoutFromStepsConversionUseCase: RecordWorkoutFromStepsConversionUseCase,
    private val setUserModeUseCase: SetUserModeUseCase,
    private val addExerciseToBankUseCase: AddExerciseToBankUseCase,
    private val updateExerciseInBankUseCase: UpdateExerciseInBankUseCase,
    private val addExerciseToSelfWorkoutUseCase: AddExerciseToSelfWorkoutUseCase,
    private val addExerciseToTrainerWorkoutUseCase: AddExerciseToTrainerWorkoutUseCase,
    private val removePlannedWorkoutItemUseCase: RemovePlannedWorkoutItemUseCase,
    private val movePlannedWorkoutItemUseCase: MovePlannedWorkoutItemUseCase,
    private val recordCompletedStudentWorkoutUseCase: RecordCompletedStudentWorkoutUseCase,
    private val exportTrainerWorkoutAsJsonUseCase: ExportTrainerWorkoutAsJsonUseCase
) : ViewModel() {
    private val transferState = MutableStateFlow(TransferState())
    private val _appNavigation = MutableStateFlow(DiplomAppNavigationState())
    val appNavigation: StateFlow<DiplomAppNavigationState> = _appNavigation.asStateFlow()
    private var importBannerClearJob: Job? = null

    val uiState: StateFlow<MainUiState> = combine(
        observeCoreMainUiInputsUseCase(),
        transferState,
        _appNavigation
    ) { core, transfer, nav ->
        MainUiState(
            today = core.activity.today,
            recentDays = core.activity.recent,
            dailyGoal = core.activity.goal,
            profile = core.activity.profile,
            weeklyChallenge = core.game.weekly,
            achievements = core.game.achievements,
            studentRewards = core.studentRewards,
            showStepsToWorkoutConversion = core.showStepsToWorkoutConversion,
            userMode = core.training.mode,
            exerciseBank = core.training.bank,
            selfWorkout = core.training.selfWorkout,
            trainerWorkout = core.training.trainerWorkout,
            exportedJson = transfer.exportedJson,
            importNotification = transfer.importNotification,
            importNotificationToken = transfer.importNotificationToken,
            openStudentTrainerSection = nav.openStudentTrainerSection
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            bootstrapGameUseCase()
            prepareTrainingStorageUseCase()
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            addStepsUseCase(steps)
        }
    }

    fun setDailyGoal(steps: Int) {
        viewModelScope.launch {
            setDailyGoalUseCase(steps)
        }
    }

    fun declineStepsToWorkoutConversion() {
        viewModelScope.launch {
            declineStepsToWorkoutConversionUseCase()
        }
    }

    fun confirmStepsToWorkoutConversion() {
        viewModelScope.launch {
            recordWorkoutFromStepsConversionUseCase()
        }
    }

    fun setUserMode(mode: AppUserMode) {
        viewModelScope.launch {
            setUserModeUseCase(mode)
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
        startWorkoutSession(items, fromTrainer = false)
    }

    fun startTrainerWorkoutSession(items: List<WorkoutExercise>) {
        startWorkoutSession(items, fromTrainer = true)
    }

    private fun startWorkoutSession(items: List<WorkoutExercise>, fromTrainer: Boolean) {
        _appNavigation.update { nav ->
            val nextId = nav.sessionInstanceId + 1
            nav.copy(
                sessionActive = true,
                sessionItems = items,
                sessionTitle = if (fromTrainer) {
                    appContext.getString(R.string.workout_session_title_trainer)
                } else {
                    appContext.getString(R.string.workout_session_title_self)
                },
                sessionFromTrainer = fromTrainer,
                sessionInstanceId = nextId
            )
        }
    }

    fun endWorkoutSession() {
        _appNavigation.update { it.copy(sessionActive = false) }
    }

    override fun onCleared() {
        importBannerClearJob?.cancel()
        super.onCleared()
    }

    private fun postImportBanner(
        notification: ImportTransferNotification?,
        autoClearMs: Long = TRANSFER_BANNER_AUTO_CLEAR_MS,
        exportedJsonPatch: String? = null
    ) {
        importBannerClearJob?.cancel()
        if (notification == null && exportedJsonPatch == null) return
        val nextToken = transferState.value.importNotificationToken + 1
        transferState.update { st ->
            st.copy(
                exportedJson = exportedJsonPatch ?: st.exportedJson,
                importNotification = notification,
                importNotificationToken = if (notification != null) nextToken else st.importNotificationToken
            )
        }
        if (notification != null && autoClearMs > 0L) {
            val capturedToken = nextToken
            importBannerClearJob = viewModelScope.launch {
                delay(autoClearMs)
                transferState.update { cur ->
                    if (cur.importNotificationToken == capturedToken) {
                        cur.copy(importNotification = null)
                    } else {
                        cur
                    }
                }
            }
        }
    }

    fun consumeOpenStudentTrainerSectionRequest() {
        _appNavigation.update { it.copy(openStudentTrainerSection = false) }
    }

    fun addExercise(title: String, description: String, defaultDurationSeconds: Int) {
        viewModelScope.launch {
            addExerciseToBankUseCase(title, description, defaultDurationSeconds)
        }
    }

    fun updateExercise(id: Long, title: String, description: String, defaultDurationSeconds: Int) {
        viewModelScope.launch {
            updateExerciseInBankUseCase(id, title, description, defaultDurationSeconds)
        }
    }

    fun addToWorkout(exercise: Exercise) {
        viewModelScope.launch {
            addExerciseToSelfWorkoutUseCase(exercise)
        }
    }

    fun addToTrainerWorkout(exercise: Exercise) {
        viewModelScope.launch {
            addExerciseToTrainerWorkoutUseCase(exercise)
        }
    }

    fun removeWorkoutItem(id: Long) {
        viewModelScope.launch {
            removePlannedWorkoutItemUseCase(id)
        }
    }

    fun moveWorkoutItem(id: Long, moveDown: Boolean) {
        viewModelScope.launch {
            movePlannedWorkoutItemUseCase(id, moveDown)
        }
    }

    fun recordStudentWorkoutCompletion(items: List<WorkoutExercise>, fromTrainerPlan: Boolean) {
        viewModelScope.launch {
            recordCompletedStudentWorkoutUseCase(items, fromTrainerPlan)
        }
    }

    fun exportTrainerWorkout() {
        viewModelScope.launch {
            val json = exportTrainerWorkoutAsJsonUseCase()
            postImportBanner(
                notification = ImportTransferNotification.ExportReady,
                exportedJsonPatch = json
            )
        }
    }

    fun importTrainerWorkout(json: String) {
        viewModelScope.launch {
            val result = importWorkoutUseCase(json)
            if (result.isSuccess) {
                postImportBanner(ImportTransferNotification.TrainerImportSuccess)
            } else {
                postImportBanner(
                    ImportTransferNotification.Failure(result.exceptionOrNull()?.message)
                )
            }
        }
    }

    fun importWorkoutFromShareIntent(rawText: String) {
        viewModelScope.launch {
            val result = applyShareImportAsStudentUseCase(rawText)
            if (result.isSuccess) {
                _appNavigation.update {
                    it.copy(
                        showModePicker = false,
                        currentDestination = AppDestinations.TRAINING,
                        sessionActive = false,
                        openStudentTrainerSection = true
                    )
                }
                postImportBanner(ImportTransferNotification.ShareImportSuccess)
            } else {
                postImportBanner(
                    ImportTransferNotification.Failure(result.exceptionOrNull()?.message)
                )
            }
        }
    }

    private companion object {
        const val TRANSFER_BANNER_AUTO_CLEAR_MS = 8_000L
    }
}

private data class TransferState(
    val exportedJson: String = "",
    val importNotification: ImportTransferNotification? = null,
    val importNotificationToken: Long = 0L
)
