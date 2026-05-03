package com.example.diplom.ui.screens

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.ui.MainUiState
import com.example.diplom.ui.UiStrings
import com.example.diplom.ui.components.AccessibleTextButton
import com.example.diplom.ui.theme.DiplomTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ADD_EXERCISE_HEADING = "Добавить упражнение в банк"

// ═══ Вспомогательные UI-компоненты ═════════════════════════════════════════════

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun GreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(12.dp))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = false,
        maxLines = 3
    )
}

// ═══ Карточка добавления упражнения ════════════════════════════════════════════

@Composable
fun AddExerciseToBankCard(
    heading: String,
    titleInput: String,
    onTitleChange: (String) -> Unit,
    descriptionInput: String,
    onDescriptionChange: (String) -> Unit,
    repsInput: String,
    onRepsChange: (String) -> Unit,
    submitLabel: String = "Добавить упражнение",
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                heading,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            StyledTextField(titleInput, onTitleChange, "Название упражнения")
            StyledTextField(descriptionInput, onDescriptionChange, "Описание")
            StyledTextField(
                value = repsInput,
                onValueChange = { onRepsChange(it.filter { ch -> ch.isDigit() }) },
                label = "Повторения по умолчанию"
            )
            GreenButton(
                submitLabel,
                onSubmit,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ═══ Карточка упражнения из банка ══════════════════════════════════════════════

@Composable
private fun ExerciseBankEntryCard(
    exercise: Exercise,
    actionLabel: String,
    onAction: () -> Unit,
    onEdit: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expandedDescription by rememberSaveable(exercise.id) { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exercise.title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                if (exercise.description.isNotBlank()) {
                    Text(
                        exercise.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = if (expandedDescription) 4 else 1
                    )
                    AccessibleTextButton(
                        onClick = { expandedDescription = !expandedDescription },
                        contentDescription = if (expandedDescription) "Свернуть описание" else "Развернуть описание"
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (expandedDescription) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (expandedDescription) "Свернуть" else "Подробнее",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Text(
                    "${exercise.defaultReps} повт.",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                modifier = Modifier.width(150.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GreenButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onEdit != null) {
                    OutlineButton(
                        text = "Редактировать",
                        onClick = onEdit,
                        icon = Icons.Default.Edit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ═══ Строка запланированного упражнения ════════════════════════════════════════

@Composable
private fun PlannedExerciseRow(
    item: WorkoutExercise,
    index: Int,
    totalCount: Int,
    onRemove: (Long) -> Unit,
    onMove: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${item.sortOrder + 1}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "${item.plannedReps} повт.",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column {
                IconButton(
                    onClick = { onMove(item.id, false) },
                    enabled = index > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Поднять выше",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onMove(item.id, true) },
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Опустить ниже",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { onRemove(item.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = UiStrings.REMOVE_FROM_LIST_A11Y,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ═══ Блок JSON для тренера ════════════════════════════════════════════════════

@Composable
private fun CopyableTrainerJsonBlock(
    exportedJson: String,
    importStatus: String?,
    clipboard: Clipboard,
    scope: CoroutineScope,
    onShareJson: (String) -> Unit
) {
    AnimatedVisibility(
        visible = exportedJson.isNotBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        UiStrings.JSON_FOR_STUDENT,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.background,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        exportedJson,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GreenButton(
                        text = "Копировать JSON",
                        icon = Icons.Default.Share,
                        onClick = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipEntry(
                                        ClipData.newPlainText("trainer_workout_json", exportedJson)
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GreenButton(
                        text = "Отправить",
                        icon = Icons.Default.Share,
                        onClick = { onShareJson(exportedJson) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    importStatus?.let {
        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

// ═══ Главный экран тренировок ══════════════════════════════════════════════════

@Composable
fun TrainingScreen(
    state: MainUiState,
    modifier: Modifier = Modifier,
    onAddExercise: (String, String, Int) -> Unit,
    onUpdateExercise: (Long, String, String, Int) -> Unit,
    onAddToWorkout: (Exercise) -> Unit,
    onAddToTrainerWorkout: (Exercise) -> Unit,
    onSaveTrainerExerciseToBank: (WorkoutExercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
    onMoveWorkoutItem: (Long, Boolean) -> Unit,
    onImportTrainerWorkout: (String) -> Unit,
    onExportTrainerWorkout: () -> Unit,
    onStartSelfWorkout: () -> Unit,
    onStartTrainerWorkout: () -> Unit
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
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val submitAddExercise: () -> Unit = {
        val reps = repsInput.toIntOrNull() ?: 10
        val editingId = editingExerciseId
        if (editingId == null) {
            onAddExercise(titleInput, descriptionInput, reps)
        } else {
            onUpdateExercise(editingId, titleInput, descriptionInput, reps)
            editingExerciseId = null
        }
        titleInput = ""
        descriptionInput = ""
        repsInput = "10"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val isTrainer = state.userMode == AppUserMode.TRAINER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isTrainer) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)
                        .border(
                            1.dp,
                            if (isTrainer) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
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
                            contentDescription = null,
                            tint = if (isTrainer) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            if (isTrainer) "Режим тренера" else "Режим ученика",
                            color = if (isTrainer) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = if (state.userMode == AppUserMode.TRAINER)
                    UiStrings.TRAINER_PLAN_HEADING
                else
                    UiStrings.STUDENT_TODAY_HEADING,
                icon = Icons.Default.FitnessCenter
            )
        }

        if (state.userMode == AppUserMode.TRAINER) {
            item {
                AddExerciseToBankCard(
                    heading = ADD_EXERCISE_HEADING,
                    titleInput = titleInput,
                    onTitleChange = { titleInput = it },
                    descriptionInput = descriptionInput,
                    onDescriptionChange = { descriptionInput = it },
                    repsInput = repsInput,
                    onRepsChange = { repsInput = it },
                    submitLabel = if (editingExerciseId == null) "Добавить упражнение" else "Сохранить",
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
                        Intent.createChooser(sendIntent, "Отправить тренировку")
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
                trainerImportInput = trainerImportInput,
                onTrainerImportChange = { trainerImportInput = it },
                studentSelfSectionVisible = studentSelfSectionVisible,
                studentTrainerSectionVisible = studentTrainerSectionVisible,
                studentAddExerciseFormVisible = studentAddExerciseFormVisible,
                submitLabel = if (editingExerciseId == null) "Добавить упражнение" else "Сохранить",
                onSelectSelfSection = {
                    studentSelfSectionVisible = true
                    studentTrainerSectionVisible = false
                },
                onSelectTrainerSection = {
                    studentTrainerSectionVisible = true
                    studentSelfSectionVisible = false
                },
                onToggleAddExerciseForm = {
                    studentAddExerciseFormVisible = !studentAddExerciseFormVisible
                },
                onSubmitStudentExercise = {
                    val reps = repsInput.toIntOrNull() ?: 10
                    val editingId = editingExerciseId
                    if (editingId == null) {
                        onAddExercise(titleInput, descriptionInput, reps)
                    } else {
                        onUpdateExercise(editingId, titleInput, descriptionInput, reps)
                        editingExerciseId = null
                    }
                    titleInput = ""
                    descriptionInput = ""
                    repsInput = "10"
                    studentAddExerciseFormVisible = false
                },
                onEditExercise = { exercise ->
                    editingExerciseId = exercise.id
                    titleInput = exercise.title
                    descriptionInput = exercise.description
                    repsInput = exercise.defaultReps.toString()
                    studentAddExerciseFormVisible = true
                },
                onAddToWorkout = onAddToWorkout,
                onSaveTrainerExerciseToBank = onSaveTrainerExerciseToBank,
                onRemoveWorkoutItem = onRemoveWorkoutItem,
                onMoveWorkoutItem = onMoveWorkoutItem,
                onImportTrainerWorkout = onImportTrainerWorkout,
                onStartSelfWorkout = onStartSelfWorkout,
                onStartTrainerWorkout = onStartTrainerWorkout
            )
        }
    }
}

// ═══ Контент тренера ═══════════════════════════════════════════════════════════

@Suppress("LongParameterList")
private fun LazyListScope.trainerTrainingContent(
    state: MainUiState,
    exerciseBank: List<Exercise>,
    bankExpanded: Boolean,
    onBankExpandedToggle: () -> Unit,
    onAddToTrainerWorkout: (Exercise) -> Unit,
    onEditExercise: (Exercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
    onMoveWorkoutItem: (Long, Boolean) -> Unit,
    onExportTrainerWorkout: () -> Unit,
    clipboard: Clipboard,
    scope: CoroutineScope,
    onShareJson: (String) -> Unit
) {
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Банк упражнений",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                }
                AccessibleTextButton(
                    onClick = onBankExpandedToggle,
                    contentDescription = if (bankExpanded)
                        UiStrings.BANK_SHOW_HIDE_A11Y_HIDE
                    else
                        UiStrings.BANK_SHOW_HIDE_A11Y_SHOW
                ) {
                    Text(
                        if (bankExpanded) "Скрыть" else "Показать",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (bankExpanded) {
        items(exerciseBank) { exercise ->
            ExerciseBankEntryCard(
                exercise = exercise,
                actionLabel = UiStrings.ACTION_FOR_STUDENT,
                onAction = { onAddToTrainerWorkout(exercise) },
                onEdit = { onEditExercise(exercise) }
            )
        }
    }

    items(state.trainerWorkout.size) { index ->
        val item = state.trainerWorkout[index]
        PlannedExerciseRow(
            item = item,
            index = index,
            totalCount = state.trainerWorkout.size,
            onRemove = onRemoveWorkoutItem,
            onMove = onMoveWorkoutItem
        )
    }

    item {
        GreenButton(
            text = "Сформировать тренировку",
            onClick = onExportTrainerWorkout,
            enabled = state.trainerWorkout.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
    }

    item {
        Text(
            "После нажатия JSON появится ниже — скопируйте и отправьте ученику.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }

    item {
        CopyableTrainerJsonBlock(
            exportedJson = state.exportedJson,
            importStatus = state.importStatus,
            clipboard = clipboard,
            scope = scope,
            onShareJson = onShareJson
        )
    }
}

// ═══ Контент ученика ═══════════════════════════════════════════════════════════

@Suppress("LongParameterList")
private fun LazyListScope.studentTrainingContent(
    state: MainUiState,
    exerciseBank: List<Exercise>,
    titleInput: String,
    onTitleChange: (String) -> Unit,
    descriptionInput: String,
    onDescriptionChange: (String) -> Unit,
    repsInput: String,
    onRepsChange: (String) -> Unit,
    trainerImportInput: String,
    onTrainerImportChange: (String) -> Unit,
    studentSelfSectionVisible: Boolean,
    studentTrainerSectionVisible: Boolean,
    studentAddExerciseFormVisible: Boolean,
    submitLabel: String,
    onSelectSelfSection: () -> Unit,
    onSelectTrainerSection: () -> Unit,
    onToggleAddExerciseForm: () -> Unit,
    onSubmitStudentExercise: () -> Unit,
    onEditExercise: (Exercise) -> Unit,
    onAddToWorkout: (Exercise) -> Unit,
    onSaveTrainerExerciseToBank: (WorkoutExercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
    onMoveWorkoutItem: (Long, Boolean) -> Unit,
    onImportTrainerWorkout: (String) -> Unit,
    onStartSelfWorkout: () -> Unit,
    onStartTrainerWorkout: () -> Unit
) {
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (studentSelfSectionVisible) {
                GreenButton(
                    "Самостоятельная",
                    onSelectSelfSection,
                    modifier = Modifier.weight(1f)
                )
                OutlineButton(
                    "От тренера",
                    onSelectTrainerSection,
                    modifier = Modifier.weight(1f)
                )
            } else if (studentTrainerSectionVisible) {
                OutlineButton(
                    "Самостоятельная",
                    onSelectSelfSection,
                    modifier = Modifier.weight(1f)
                )
                GreenButton(
                    "От тренера",
                    onSelectTrainerSection,
                    modifier = Modifier.weight(1f)
                )
            } else {
                OutlineButton(
                    "Самостоятельная",
                    onSelectSelfSection,
                    modifier = Modifier.weight(1f)
                )
                OutlineButton(
                    "От тренера",
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Банк упражнений", Icons.Default.FitnessCenter)
                    AccessibleTextButton(
                        onClick = onToggleAddExerciseForm,
                        contentDescription = UiStrings.ADD_EXERCISE_FORM_A11Y
                    ) {
                        Text(UiStrings.ADD_OWN_EXERCISE, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                    heading = ADD_EXERCISE_HEADING,
                    titleInput = titleInput,
                    onTitleChange = onTitleChange,
                    descriptionInput = descriptionInput,
                    onDescriptionChange = onDescriptionChange,
                    repsInput = repsInput,
                    onRepsChange = onRepsChange,
                    submitLabel = submitLabel,
                    onSubmit = onSubmitStudentExercise
                )
            }
        }

        items(exerciseBank) { exercise ->
            ExerciseBankEntryCard(
                exercise = exercise,
                actionLabel = UiStrings.ACTION_TO_WORKOUT,
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
                text = "Начать тренировку",
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
                label = "JSON тренировки от тренера"
            )
        }
        item {
            GreenButton(
                text = "Импортировать",
                onClick = { onImportTrainerWorkout(trainerImportInput) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            state.importStatus?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
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
                GreenButton(
                    text = "Сохранить в мой банк",
                    onClick = { onSaveTrainerExerciseToBank(item) },
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            GreenButton(
                text = "Начать тренировку от тренера",
                onClick = onStartTrainerWorkout,
                enabled = state.trainerWorkout.isNotEmpty(),
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ═══ Экран сессии тренировки ═══════════════════════════════════════════════════

@Composable
fun WorkoutSessionScreen(
    sessionInstanceId: Int,
    title: String,
    items: List<WorkoutExercise>,
    modifier: Modifier = Modifier,
    onFinish: () -> Unit,
    onWorkoutCompleted: (List<WorkoutExercise>) -> Unit = {}
) {
    val total = items.size
    var currentIndex by rememberSaveable(sessionInstanceId) { mutableIntStateOf(0) }
    var remainingSeconds by rememberSaveable(sessionInstanceId) {
        mutableIntStateOf(items.firstOrNull()?.plannedReps ?: 0)
    }
    var timerRunning by rememberSaveable(sessionInstanceId) { mutableStateOf(false) }
    var sessionFinished by rememberSaveable(sessionInstanceId) { mutableStateOf(false) }
    var completionLogged by rememberSaveable(sessionInstanceId) { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable(sessionInstanceId) { mutableStateOf(false) }

    LaunchedEffect(sessionFinished, total, sessionInstanceId) {
        if (sessionFinished && total > 0 && !completionLogged) {
            completionLogged = true
            onWorkoutCompleted(items)
        }
    }

    LaunchedEffect(timerRunning, currentIndex, remainingSeconds, total, sessionFinished) {
        if (!timerRunning || sessionFinished || total == 0) return@LaunchedEffect
        if (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
            return@LaunchedEffect
        }
        val nextIndex = currentIndex + 1
        if (nextIndex >= total) {
            timerRunning = false
            sessionFinished = true
        } else {
            currentIndex = nextIndex
            remainingSeconds = items[nextIndex].plannedReps.coerceAtLeast(1)
        }
    }

    val doneCount = when {
        total == 0 -> 0
        sessionFinished -> total
        else -> currentIndex
    }

    val progressAnim by animateFloatAsState(
        targetValue = if (total == 0) 0f else doneCount / total.toFloat(),
        animationSpec = tween(600),
        label = "progress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(title, Icons.Default.FitnessCenter)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Выполнено: $doneCount из $total",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            "${if (total == 0) 0 else (doneCount * 100 / total)}%",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        if (total == 0) {
            item {
                Text("На сегодня нет упражнений.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            item {
                OutlineButton("Назад", onFinish, modifier = Modifier.fillMaxWidth())
            }
            return@LazyColumn
        }

        if (sessionFinished) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 48.sp)
                        Text(
                            "Тренировка завершена!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                        Text(
                            "Выполнено $doneCount из $total упражнений",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        GreenButton(
                            "К списку тренировок",
                            onFinish,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            return@LazyColumn
        }

        val current = items[currentIndex]
        val currentDescription = current.description
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Текущее упражнение",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        current.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    if (currentDescription.isNotBlank()) {
                        Text(
                            text = currentDescription,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        val timerColor by animateColorAsState(
                            targetValue = if (remainingSeconds <= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            animationSpec = tween(500),
                            label = "timerColor"
                        )
                        Text(
                            "$remainingSeconds сек",
                            color = timerColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    GreenButton(
                        text = if (timerRunning) "Пауза" else "Возобновить отсчёт",
                        onClick = { timerRunning = !timerRunning },
                        icon = if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlineButton(
                        text = "Закрыть режим тренировки",
                        onClick = { showExitConfirmation = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Точно выйти?") },
            text = { Text("Прогресс тренировки будет сброшен") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        onFinish()
                    }
                ) {
                    Text("Выйти")
                }
            },
            dismissButton = {
                OutlineButton(
                    text = "Отмена",
                    onClick = { showExitConfirmation = false }
                )
            }
        )
    }
}

// ═══ Preview ═══════════════════════════════════════════════════════════════════

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
            onStartTrainerWorkout = {}
        )
    }
}