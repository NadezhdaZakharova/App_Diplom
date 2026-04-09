package com.example.diplom.ui.screens

import android.content.ClipData
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
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
import com.example.diplom.ui.theme.DarkBackground
import com.example.diplom.ui.theme.DarkSurface
import com.example.diplom.ui.theme.DiplomTheme
import com.example.diplom.ui.theme.GreenPrimary
import com.example.diplom.ui.theme.OrangeAccent
import com.example.diplom.ui.theme.TextPrimaryDark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ADD_EXERCISE_HEADING = "Добавить упражнение в банк"

// ─── Вспомогательные UI-компоненты ───────────────────────────────────────────

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimaryDark
        )
    }
}

@Composable
private fun GreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GreenPrimary,
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFF1E2A38),
            disabledContentColor = Color(0xFF6B7A8D)
        ),
        modifier = modifier.defaultMinSize(minHeight = 48.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
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
            contentColor = GreenPrimary
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .border(1.dp, GreenPrimary, RoundedCornerShape(12.dp))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.Medium)
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
        label = { Text(label, color = Color(0xFF6B7A8D)) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = Color(0xFF1E2A38),
            focusedTextColor = TextPrimaryDark,
            unfocusedTextColor = TextPrimaryDark,
            cursorColor = GreenPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// ─── Карточка добавления упражнения ──────────────────────────────────────────

@Composable
fun AddExerciseToBankCard(
    heading: String,
    titleInput: String,
    onTitleChange: (String) -> Unit,
    descriptionInput: String,
    onDescriptionChange: (String) -> Unit,
    repsInput: String,
    onRepsChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2A38))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                heading,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark,
                fontSize = 15.sp
            )
            StyledTextField(titleInput, onTitleChange, "Название")
            StyledTextField(descriptionInput, onDescriptionChange, "Описание")
            StyledTextField(
                value = repsInput,
                onValueChange = { onRepsChange(it.filter { ch -> ch.isDigit() }) },
                label = "Повторения по умолчанию"
            )
            GreenButton("Добавить", onSubmit, icon = Icons.Default.Add)
        }
    }
}

// ─── Карточка упражнения из банка ────────────────────────────────────────────

@Composable
private fun ExerciseBankEntryCard(
    exercise: Exercise,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2A38))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка-аватар упражнения
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F2A1A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exercise.title,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark,
                    fontSize = 14.sp
                )
                if (exercise.description.isNotBlank()) {
                    Text(
                        exercise.description,
                        color = Color(0xFF6B7A8D),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Text(
                    "${exercise.defaultReps} повт.",
                    color = GreenPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(8.dp))
            GreenButton(
                text = actionLabel,
                onClick = onAction,
                modifier = Modifier.defaultMinSize(minWidth = 80.dp)
            )
        }
    }
}

// ─── Строка запланированного упражнения ──────────────────────────────────────

@Composable
private fun PlannedExerciseRow(
    item: WorkoutExercise,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2A38))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F2A1A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${item.sortOrder + 1}",
                    color = GreenPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                item.title,
                color = TextPrimaryDark,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F2A1A))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("${item.plannedReps} повт.", color = GreenPrimary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onRemove(item.id) }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = UiStrings.REMOVE_FROM_LIST_A11Y,
                    tint = Color(0xFF6B7A8D),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── Блок JSON для тренера ────────────────────────────────────────────────────

@Composable
private fun CopyableTrainerJsonBlock(
    exportedJson: String,
    importStatus: String?,
    clipboard: Clipboard,
    scope: CoroutineScope
) {
    AnimatedVisibility(
        visible = exportedJson.isNotBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2A1A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    UiStrings.JSON_FOR_STUDENT,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary
                )
                Text(
                    exportedJson,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7A8D)
                )
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
                    }
                )
            }
        }
    }
    importStatus?.let {
        Text(it, color = Color(0xFF6B7A8D), fontSize = 13.sp)
    }
}

// ─── Главный экран тренировок ─────────────────────────────────────────────────

@Composable
fun TrainingScreen(
    state: MainUiState,
    modifier: Modifier = Modifier,
    onAddExercise: (String, String, Int) -> Unit,
    onAddToWorkout: (Exercise) -> Unit,
    onAddToTrainerWorkout: (Exercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
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
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val submitAddExercise: () -> Unit = {
        onAddExercise(titleInput, descriptionInput, repsInput.toIntOrNull() ?: 10)
        titleInput = ""
        descriptionInput = ""
        repsInput = "10"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Режим-бейдж вверху
        item {
            val isTrainer = state.userMode == AppUserMode.TRAINER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isTrainer) Color(0xFF1A1060) else Color(0xFF0F2A1A))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isTrainer) Icons.Default.Person else Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = if (isTrainer) Color(0xFF818CF8) else GreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (isTrainer) "Режим тренера" else "Режим ученика",
                            color = if (isTrainer) Color(0xFF818CF8) else GreenPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
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
                    onSubmit = submitAddExercise
                )
            }

            trainerTrainingContent(
                state = state,
                bankExpanded = bankExpanded,
                onBankExpandedToggle = { bankExpanded = !bankExpanded },
                onAddToTrainerWorkout = onAddToTrainerWorkout,
                onRemoveWorkoutItem = onRemoveWorkoutItem,
                onExportTrainerWorkout = onExportTrainerWorkout,
                clipboard = clipboard,
                scope = scope
            )
        } else {
            studentTrainingContent(
                state = state,
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
                    submitAddExercise()
                    studentAddExerciseFormVisible = false
                },
                onAddToWorkout = onAddToWorkout,
                onRemoveWorkoutItem = onRemoveWorkoutItem,
                onImportTrainerWorkout = onImportTrainerWorkout,
                onStartSelfWorkout = onStartSelfWorkout,
                onStartTrainerWorkout = onStartTrainerWorkout
            )
        }
    }
}

// ─── Контент тренера ──────────────────────────────────────────────────────────

@Suppress("LongParameterList")
private fun LazyListScope.trainerTrainingContent(
    state: MainUiState,
    bankExpanded: Boolean,
    onBankExpandedToggle: () -> Unit,
    onAddToTrainerWorkout: (Exercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
    onExportTrainerWorkout: () -> Unit,
    clipboard: Clipboard,
    scope: CoroutineScope
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2A38))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = GreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Банк упражнений",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
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
                        color = GreenPrimary
                    )
                }
            }
        }
    }

    if (bankExpanded) {
        items(state.exerciseBank) { exercise ->
            ExerciseBankEntryCard(
                exercise = exercise,
                actionLabel = UiStrings.ACTION_FOR_STUDENT,
                onAction = { onAddToTrainerWorkout(exercise) }
            )
        }
    }

    items(state.trainerWorkout) { item ->
        PlannedExerciseRow(item = item, onRemove = onRemoveWorkoutItem)
    }

    item {
        GreenButton(
            text = "Сформировать тренировку",
            onClick = onExportTrainerWorkout,
            enabled = state.trainerWorkout.isNotEmpty(),
            icon = Icons.Default.Share,
            modifier = Modifier.fillMaxWidth()
        )
    }
    item {
        Text(
            "После нажатия JSON появится ниже — скопируйте и отправьте ученику.",
            color = Color(0xFF6B7A8D),
            fontSize = 13.sp
        )
    }
    item {
        CopyableTrainerJsonBlock(
            exportedJson = state.exportedJson,
            importStatus = state.importStatus,
            clipboard = clipboard,
            scope = scope
        )
    }
}

// ─── Контент ученика ──────────────────────────────────────────────────────────

@Suppress("LongParameterList")
private fun LazyListScope.studentTrainingContent(
    state: MainUiState,
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
    onSelectSelfSection: () -> Unit,
    onSelectTrainerSection: () -> Unit,
    onToggleAddExerciseForm: () -> Unit,
    onSubmitStudentExercise: () -> Unit,
    onAddToWorkout: (Exercise) -> Unit,
    onRemoveWorkoutItem: (Long) -> Unit,
    onImportTrainerWorkout: (String) -> Unit,
    onStartSelfWorkout: () -> Unit,
    onStartTrainerWorkout: () -> Unit
) {
    // Кнопки выбора режима
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

    // Самостоятельная секция
    if (studentSelfSectionVisible) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2A38))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Банк упражнений", Icons.Default.FitnessCenter)
                    AccessibleTextButton(
                        onClick = onToggleAddExerciseForm,
                        contentDescription = UiStrings.ADD_EXERCISE_FORM_A11Y
                    ) {
                        Text(UiStrings.ADD_OWN_EXERCISE, color = GreenPrimary)
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
                    onSubmit = onSubmitStudentExercise
                )
            }
        }

        items(state.exerciseBank) { exercise ->
            ExerciseBankEntryCard(
                exercise = exercise,
                actionLabel = UiStrings.ACTION_TO_WORKOUT,
                onAction = { onAddToWorkout(exercise) }
            )
        }

        items(state.selfWorkout) { item ->
            PlannedExerciseRow(item = item, onRemove = onRemoveWorkoutItem)
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

    // Секция от тренера
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
                Text(it, color = Color(0xFF6B7A8D), fontSize = 13.sp)
            }
        }
        items(state.trainerWorkout) { item ->
            PlannedExerciseRow(item = item, onRemove = onRemoveWorkoutItem)
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

// ─── Экран сессии тренировки ──────────────────────────────────────────────────

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
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(title, Icons.Default.FitnessCenter) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2A38))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Выполнено: $doneCount из $total",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${if (total == 0) 0 else (doneCount * 100 / total)}%",
                            color = GreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = GreenPrimary,
                        trackColor = Color(0xFF1E2A38),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        if (total == 0) {
            item { Text("На сегодня нет упражнений.", color = Color(0xFF6B7A8D)) }
            item {
                OutlineButton("Назад", onFinish, modifier = Modifier.fillMaxWidth())
            }
            return@LazyColumn
        }

        if (sessionFinished) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2A1A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 40.sp)
                        Text(
                            "Тренировка завершена!",
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            "Выполнено $doneCount из $total упражнений",
                            color = Color(0xFF6B7A8D),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
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
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2A38))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Текущее упражнение",
                        color = Color(0xFF6B7A8D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        current.title,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    // Таймер
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        val timerColor by animateColorAsState(
                            targetValue = if (remainingSeconds <= 5) OrangeAccent else GreenPrimary,
                            animationSpec = tween(500),
                            label = "timerColor"
                        )
                        Text(
                            "$remainingSeconds сек",
                            color = timerColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    GreenButton(
                        text = if (timerRunning) "Идёт..." else "Запустить отсчёт",
                        onClick = { timerRunning = true },
                        enabled = !timerRunning,
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

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
            onAddToWorkout = {},
            onAddToTrainerWorkout = {},
            onRemoveWorkoutItem = {},
            onImportTrainerWorkout = {},
            onExportTrainerWorkout = {},
            onStartSelfWorkout = {},
            onStartTrainerWorkout = {}
        )
    }
}