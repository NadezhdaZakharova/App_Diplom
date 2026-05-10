package com.example.diplom.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.ui.MainUiState
import com.example.diplom.ui.theme.DiplomTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TrainingScreen(
    state: MainUiState,
    modifier: Modifier = Modifier,
    onAddExercise: (String, String, Int) -> Unit,
    onUpdateExercise: (Long, String, String, Int) -> Unit,
    onConsumedOpenStudentTrainerSection: () -> Unit = {},
    onAddToWorkout: (Exercise) -> Unit,
    onAddToTrainerWorkout: (Exercise) -> Unit,
    onSaveTrainerExerciseToBank: (WorkoutExercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
    onMoveWorkoutItem: (Long, Boolean) -> Unit,
    onImportTrainerWorkout: (String) -> Unit,
    onExportTrainerWorkout: () -> Unit,
    onStartSelfWorkout: () -> Unit,
    onStartTrainerWorkout: () -> Unit,
    onSetDailyGoal: (Int) -> Unit
) {
    var titleInput by rememberSaveable { mutableStateOf("") }
    var descriptionInput by rememberSaveable { mutableStateOf("") }
    var repsInput by rememberSaveable { mutableStateOf("10") }
    var trainerImportInput by rememberSaveable { mutableStateOf("") }
    var bankExpanded by rememberSaveable { mutableStateOf(false) }
    var studentSelfSectionVisible by rememberSaveable { mutableStateOf(false) }
    var studentTrainerSectionVisible by rememberSaveable { mutableStateOf(false) }
    var studentAddExerciseFormVisible by rememberSaveable { mutableStateOf(false) }
    var editingExerciseId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editSaveShowingAck by remember { mutableStateOf(false) }
    val trainerBankSavePhases = remember { mutableStateMapOf<Long, TrainerBankSaveButtonPhase>() }
    val listState = rememberLazyListState()
    val exerciseFormBringIntoView = remember { BringIntoViewRequester() }
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var studentStepGoalDraft by rememberSaveable { mutableStateOf("") }
    var studentStepGoalError by remember { mutableStateOf<String?>(null) }
    var stepGoalSavedAck by remember { mutableStateOf(false) }
    var studentStepGoalSectionExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.dailyGoal) {
        studentStepGoalDraft = state.dailyGoal.toString()
        studentStepGoalError = null
    }

    LaunchedEffect(stepGoalSavedAck) {
        if (!stepGoalSavedAck) return@LaunchedEffect
        delay(2_000)
        stepGoalSavedAck = false
    }

    LaunchedEffect(state.openStudentTrainerSection) {
        if (state.openStudentTrainerSection) {
            studentTrainerSectionVisible = true
            studentSelfSectionVisible = false
            onConsumedOpenStudentTrainerSection()
        }
    }

    LaunchedEffect(
        editingExerciseId,
        state.userMode,
        studentSelfSectionVisible,
        studentAddExerciseFormVisible
    ) {
        if (editingExerciseId == null) return@LaunchedEffect
        when (state.userMode) {
            AppUserMode.TRAINER -> {
                delay(80)
                exerciseFormBringIntoView.bringIntoView()
            }
            AppUserMode.STUDENT -> {
                if (!studentSelfSectionVisible || !studentAddExerciseFormVisible) return@LaunchedEffect
                delay(80)
                exerciseFormBringIntoView.bringIntoView()
            }
        }
    }

    LaunchedEffect(editSaveShowingAck) {
        if (!editSaveShowingAck) return@LaunchedEffect
        delay(1_000)
        editingExerciseId = null
        editSaveShowingAck = false
        titleInput = ""
        descriptionInput = ""
        repsInput = "10"
        studentAddExerciseFormVisible = false
    }

    val bankSubmitLabel = when {
        editingExerciseId != null && editSaveShowingAck -> stringResource(R.string.bank_submit_saved)
        editingExerciseId != null -> stringResource(R.string.bank_submit_save)
        else -> stringResource(R.string.bank_submit_add)
    }
    val durationSecondsFieldError = defaultDurationFieldErrorOrNull(repsInput)
    val bankSubmitEnabled = !editSaveShowingAck && durationSecondsFieldError == null
    val shareWorkoutChooserTitle = stringResource(R.string.share_workout_chooser_title)
    val studentStepGoalInvalidMessage = stringResource(R.string.student_step_goal_invalid)
    val studentStepGoalApplyLabel = stringResource(R.string.student_step_goal_apply)
    val studentStepGoalSavedLabel = stringResource(R.string.student_step_goal_saved)
    val stepGoalApplyButtonLabel =
        if (stepGoalSavedAck) studentStepGoalSavedLabel else studentStepGoalApplyLabel

    val submitExerciseBankForm: (onAfter: () -> Unit) -> Unit = { onAfter ->
        if (!editSaveShowingAck) {
            val seconds = parsedDurationSecondsOrNull(repsInput)
            if (seconds != null) {
                val editingId = editingExerciseId
                if (editingId == null) {
                    onAddExercise(titleInput, descriptionInput, seconds)
                    titleInput = ""
                    descriptionInput = ""
                    repsInput = "10"
                    onAfter()
                } else {
                    onUpdateExercise(editingId, titleInput, descriptionInput, seconds)
                    editSaveShowingAck = true
                }
            }
        }
    }

    val submitAddExercise: () -> Unit = { submitExerciseBankForm { } }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val isTrainer = state.userMode == AppUserMode.TRAINER
            val modeStudent = stringResource(R.string.mode_chip_student)
            val modeTrainer = stringResource(R.string.mode_chip_trainer)
            val studentIconA11y = stringResource(R.string.a11y_mode_badge_student)
            val trainerIconA11y = stringResource(R.string.a11y_mode_badge_trainer)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isTrainer) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                        .border(
                            1.dp,
                            if (isTrainer) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isTrainer) Icons.Default.Person else Icons.Default.FitnessCenter,
                            contentDescription = if (isTrainer) trainerIconA11y else studentIconA11y,
                            tint = if (isTrainer) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            if (isTrainer) modeTrainer else modeStudent,
                            color = if (isTrainer) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = if (state.userMode == AppUserMode.TRAINER) {
                    stringResource(R.string.trainer_plan_heading)
                } else {
                    stringResource(R.string.student_today_heading)
                },
                icon = Icons.Default.FitnessCenter
            )
        }

        if (state.userMode == AppUserMode.TRAINER) {
            item {
                AddExerciseToBankCard(
                    heading = stringResource(R.string.add_exercise_bank_heading),
                    titleInput = titleInput,
                    onTitleChange = { titleInput = it },
                    descriptionInput = descriptionInput,
                    onDescriptionChange = { descriptionInput = it },
                    repsInput = repsInput,
                    onRepsChange = { repsInput = it },
                    durationSecondsError = durationSecondsFieldError,
                    bringIntoViewRequester = exerciseFormBringIntoView,
                    submitLabel = bankSubmitLabel,
                    submitEnabled = bankSubmitEnabled,
                    onSubmit = submitAddExercise
                )
            }

            trainerTrainingContent(
                state = state,
                exerciseBank = state.exerciseBank,
                bankExpanded = bankExpanded,
                onBankExpandedToggle = { bankExpanded = !bankExpanded },
                onAddToTrainerWorkout = onAddToTrainerWorkout,
                onEditExercise = { exercise ->
                    editSaveShowingAck = false
                    editingExerciseId = exercise.id
                    titleInput = exercise.title
                    descriptionInput = exercise.description
                    repsInput = exercise.defaultReps.toString()
                },
                onRemoveWorkoutItem = onRemoveWorkoutItem,
                onMoveWorkoutItem = onMoveWorkoutItem,
                onExportTrainerWorkout = onExportTrainerWorkout,
                clipboard = clipboard,
                scope = scope,
                onShareJson = { payload ->
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, payload)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            sendIntent,
                            shareWorkoutChooserTitle
                        )
                    )
                }
            )
        } else {
            studentTrainingContent(
                state = state,
                exerciseBank = state.exerciseBank,
                titleInput = titleInput,
                onTitleChange = { titleInput = it },
                descriptionInput = descriptionInput,
                onDescriptionChange = { descriptionInput = it },
                repsInput = repsInput,
                onRepsChange = { repsInput = it },
                durationSecondsFieldError = durationSecondsFieldError,
                exerciseFormBringIntoView = exerciseFormBringIntoView,
                trainerImportInput = trainerImportInput,
                onTrainerImportChange = { trainerImportInput = it },
                studentSelfSectionVisible = studentSelfSectionVisible,
                studentTrainerSectionVisible = studentTrainerSectionVisible,
                studentAddExerciseFormVisible = studentAddExerciseFormVisible,
                submitLabel = bankSubmitLabel,
                submitEnabled = bankSubmitEnabled,
                onSelectSelfSection = {
                    if (studentSelfSectionVisible) {
                        studentSelfSectionVisible = false
                    } else {
                        studentSelfSectionVisible = true
                        studentTrainerSectionVisible = false
                    }
                },
                onSelectTrainerSection = {
                    if (studentTrainerSectionVisible) {
                        studentTrainerSectionVisible = false
                    } else {
                        studentTrainerSectionVisible = true
                        studentSelfSectionVisible = false
                    }
                },
                onToggleAddExerciseForm = {
                    studentAddExerciseFormVisible = !studentAddExerciseFormVisible
                },
                onSubmitStudentExercise = {
                    submitExerciseBankForm {
                        studentAddExerciseFormVisible = false
                    }
                },
                onEditExercise = { exercise ->
                    editSaveShowingAck = false
                    editingExerciseId = exercise.id
                    titleInput = exercise.title
                    descriptionInput = exercise.description
                    repsInput = exercise.defaultReps.toString()
                    studentAddExerciseFormVisible = true
                },
                onAddToWorkout = onAddToWorkout,
                trainerBankSaveButtonPhase = { id ->
                    trainerBankSavePhases[id] ?: TrainerBankSaveButtonPhase.Save
                },
                onSaveTrainerExerciseToBank = { item ->
                    onSaveTrainerExerciseToBank(item)
                    trainerBankSavePhases[item.id] = TrainerBankSaveButtonPhase.SavedAck
                    scope.launch {
                        delay(1_000)
                        trainerBankSavePhases[item.id] = TrainerBankSaveButtonPhase.Hidden
                    }
                },
                onRemoveWorkoutItem = onRemoveWorkoutItem,
                onMoveWorkoutItem = onMoveWorkoutItem,
                onImportTrainerWorkout = onImportTrainerWorkout,
                onStartSelfWorkout = onStartSelfWorkout,
                onStartTrainerWorkout = onStartTrainerWorkout,
                studentStepGoalDraft = studentStepGoalDraft,
                onStudentStepGoalDraftChange = { value ->
                    studentStepGoalDraft = value.filter { it.isDigit() }.take(5)
                    studentStepGoalError = null
                },
                studentStepGoalError = studentStepGoalError,
                stepGoalApplyLabel = stepGoalApplyButtonLabel,
                stepGoalApplyEnabled = !stepGoalSavedAck,
                studentStepGoalSectionExpanded = studentStepGoalSectionExpanded,
                onStudentStepGoalSectionExpandedChange = { studentStepGoalSectionExpanded = it },
                onApplyStudentStepGoal = {
                    if (!stepGoalSavedAck) {
                        val n = studentStepGoalDraft.toIntOrNull()
                        if (n == null || n !in 2000..30000) {
                            studentStepGoalError = studentStepGoalInvalidMessage
                        } else {
                            studentStepGoalError = null
                            onSetDailyGoal(n)
                            stepGoalSavedAck = true
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainingScreenPreview() {
    DiplomTheme {
        TrainingScreen(
            state = MainUiState(
                userMode = AppUserMode.STUDENT,
                exerciseBank = listOf(
                    Exercise(id = 1L, title = "Приседания", description = "3 подхода в комфортном темпе", defaultReps = 15),
                    Exercise(id = 2L, title = "Планка", description = "Удержание корпуса", defaultReps = 45)
                ),
                selfWorkout = listOf(
                    WorkoutExercise(id = 10L, dateIso = "2026-04-01", exerciseId = 1L, title = "Приседания", plannedReps = 15, sortOrder = 0),
                    WorkoutExercise(id = 11L, dateIso = "2026-04-01", exerciseId = 2L, title = "Планка", plannedReps = 45, sortOrder = 1)
                ),
                trainerWorkout = emptyList()
            ),
            onAddExercise = { _, _, _ -> },
            onUpdateExercise = { _, _, _, _ -> },
            onAddToWorkout = {},
            onSaveTrainerExerciseToBank = {},
            onAddToTrainerWorkout = {},
            onRemoveWorkoutItem = {},
            onMoveWorkoutItem = { _, _ -> },
            onImportTrainerWorkout = {},
            onExportTrainerWorkout = {},
            onStartSelfWorkout = {},
            onStartTrainerWorkout = {},
            onSetDailyGoal = {}
        )
    }
}
