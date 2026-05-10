package com.example.diplom.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.ui.MainUiState
import com.example.diplom.ui.components.AccessibleTextButton
import androidx.compose.foundation.relocation.BringIntoViewRequester

@Suppress("LongParameterList")
internal fun LazyListScope.studentTrainingContent(
    state: MainUiState,
    exerciseBank: List<Exercise>,
    titleInput: String,
    onTitleChange: (String) -> Unit,
    descriptionInput: String,
    onDescriptionChange: (String) -> Unit,
    repsInput: String,
    onRepsChange: (String) -> Unit,
    durationSecondsFieldError: String?,
    exerciseFormBringIntoView: BringIntoViewRequester,
    trainerImportInput: String,
    onTrainerImportChange: (String) -> Unit,
    studentSelfSectionVisible: Boolean,
    studentTrainerSectionVisible: Boolean,
    studentAddExerciseFormVisible: Boolean,
    submitLabel: String,
    submitEnabled: Boolean,
    onSelectSelfSection: () -> Unit,
    onSelectTrainerSection: () -> Unit,
    onToggleAddExerciseForm: () -> Unit,
    onSubmitStudentExercise: () -> Unit,
    onEditExercise: (Exercise) -> Unit,
    onAddToWorkout: (Exercise) -> Unit,
    trainerBankSaveButtonPhase: (Long) -> TrainerBankSaveButtonPhase,
    onSaveTrainerExerciseToBank: (WorkoutExercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
    onMoveWorkoutItem: (Long, Boolean) -> Unit,
    onImportTrainerWorkout: (String) -> Unit,
    onStartSelfWorkout: () -> Unit,
    onStartTrainerWorkout: () -> Unit,
    studentStepGoalDraft: String,
    onStudentStepGoalDraftChange: (String) -> Unit,
    studentStepGoalError: String?,
    stepGoalApplyLabel: String,
    stepGoalApplyEnabled: Boolean,
    studentStepGoalSectionExpanded: Boolean,
    onStudentStepGoalSectionExpandedChange: (Boolean) -> Unit,
    onApplyStudentStepGoal: () -> Unit
) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (studentSelfSectionVisible) {
                GreenButton(
                    stringResource(R.string.student_section_self),
                    onSelectSelfSection,
                    modifier = Modifier.weight(1f)
                )
                OutlineButton(
                    stringResource(R.string.student_section_trainer),
                    onSelectTrainerSection,
                    modifier = Modifier.weight(1f)
                )
            } else if (studentTrainerSectionVisible) {
                OutlineButton(
                    stringResource(R.string.student_section_self),
                    onSelectSelfSection,
                    modifier = Modifier.weight(1f)
                )
                GreenButton(
                    stringResource(R.string.student_section_trainer),
                    onSelectTrainerSection,
                    modifier = Modifier.weight(1f)
                )
            } else {
                OutlineButton(
                    stringResource(R.string.student_section_self),
                    onSelectSelfSection,
                    modifier = Modifier.weight(1f)
                )
                OutlineButton(
                    stringResource(R.string.student_section_trainer),
                    onSelectTrainerSection,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (studentSelfSectionVisible) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.exercise_bank_title),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    AccessibleTextButton(
                        onClick = onToggleAddExerciseForm,
                        contentDescription = stringResource(R.string.add_exercise_form_a11y)
                    ) {
                        Text(
                            stringResource(R.string.add_own_exercise),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = studentAddExerciseFormVisible,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                AddExerciseToBankCard(
                    heading = stringResource(R.string.add_exercise_bank_heading),
                    titleInput = titleInput,
                    onTitleChange = onTitleChange,
                    descriptionInput = descriptionInput,
                    onDescriptionChange = onDescriptionChange,
                    repsInput = repsInput,
                    onRepsChange = onRepsChange,
                    durationSecondsError = durationSecondsFieldError,
                    bringIntoViewRequester = exerciseFormBringIntoView,
                    submitLabel = submitLabel,
                    submitEnabled = submitEnabled,
                    onSubmit = onSubmitStudentExercise
                )
            }
        }

        items(exerciseBank) { exercise ->
            ExerciseBankEntryCard(
                exercise = exercise,
                actionLabel = stringResource(R.string.action_to_workout),
                onAction = { onAddToWorkout(exercise) },
                onEdit = { onEditExercise(exercise) }
            )
        }

        items(state.selfWorkout.size) { index ->
            val item = state.selfWorkout[index]
            PlannedExerciseRow(
                item = item,
                index = index,
                totalCount = state.selfWorkout.size,
                onRemove = onRemoveWorkoutItem,
                onMove = onMoveWorkoutItem
            )
        }

        item {
            GreenButton(
                text = stringResource(R.string.start_workout),
                onClick = onStartSelfWorkout,
                enabled = state.selfWorkout.isNotEmpty(),
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (studentTrainerSectionVisible) {
        item {
            StyledTextField(
                value = trainerImportInput,
                onValueChange = onTrainerImportChange,
                label = stringResource(R.string.trainer_import_json_label)
            )
        }
        item {
            GreenButton(
                text = stringResource(R.string.import_action),
                onClick = { onImportTrainerWorkout(trainerImportInput) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(state.trainerWorkout.size) { index ->
            val item = state.trainerWorkout[index]
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlannedExerciseRow(
                    item = item,
                    index = index,
                    totalCount = state.trainerWorkout.size,
                    onRemove = onRemoveWorkoutItem,
                    onMove = onMoveWorkoutItem
                )
                when (trainerBankSaveButtonPhase(item.id)) {
                    TrainerBankSaveButtonPhase.Save -> GreenButton(
                        text = stringResource(R.string.save_to_my_bank),
                        onClick = { onSaveTrainerExerciseToBank(item) },
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TrainerBankSaveButtonPhase.SavedAck -> GreenButton(
                        text = stringResource(R.string.bank_submit_saved),
                        onClick = {},
                        enabled = false,
                        icon = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TrainerBankSaveButtonPhase.Hidden -> {}
                }
            }
        }
        item {
            GreenButton(
                text = stringResource(R.string.start_trainer_workout),
                onClick = onStartTrainerWorkout,
                enabled = state.trainerWorkout.isNotEmpty(),
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    item {
        val toggleA11y = stringResource(
            if (studentStepGoalSectionExpanded) {
                R.string.student_step_goal_toggle_collapse_a11y
            } else {
                R.string.student_step_goal_toggle_expand_a11y
            }
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onStudentStepGoalSectionExpandedChange(!studentStepGoalSectionExpanded)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.student_step_goal_heading),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (studentStepGoalSectionExpanded) {
                            Icons.Filled.ExpandLess
                        } else {
                            Icons.Filled.ExpandMore
                        },
                        contentDescription = toggleA11y,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                AnimatedVisibility(
                    visible = studentStepGoalSectionExpanded,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StyledTextField(
                            value = studentStepGoalDraft,
                            onValueChange = onStudentStepGoalDraftChange,
                            label = stringResource(R.string.student_step_goal_label)
                        )
                        if (studentStepGoalError != null) {
                            Text(
                                text = studentStepGoalError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                        GreenButton(
                            text = stepGoalApplyLabel,
                            onClick = onApplyStudentStepGoal,
                            enabled = stepGoalApplyEnabled,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
