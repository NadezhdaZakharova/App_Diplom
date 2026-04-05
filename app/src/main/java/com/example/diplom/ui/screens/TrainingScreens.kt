package com.example.diplom.ui.screens

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(heading, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = titleInput,
                onValueChange = onTitleChange,
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = descriptionInput,
                onValueChange = onDescriptionChange,
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = repsInput,
                onValueChange = { onRepsChange(it.filter { ch -> ch.isDigit() }) },
                label = { Text("Повторения по умолчанию") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onSubmit,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Добавить")
            }
        }
    }
}

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
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
        }

        item {
            Text(
                if (state.userMode == AppUserMode.TRAINER) {
                    UiStrings.TRAINER_PLAN_HEADING
                } else {
                    UiStrings.STUDENT_TODAY_HEADING
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        when (state.userMode) {
            AppUserMode.TRAINER -> trainerTrainingContent(
                state = state,
                bankExpanded = bankExpanded,
                onBankExpandedToggle = { bankExpanded = !bankExpanded },
                onAddToTrainerWorkout = onAddToTrainerWorkout,
                onRemoveWorkoutItem = onRemoveWorkoutItem,
                onExportTrainerWorkout = onExportTrainerWorkout,
                clipboard = clipboard,
                scope = scope
            )
            AppUserMode.STUDENT -> studentTrainingContent(
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
                onToggleAddExerciseForm = { studentAddExerciseFormVisible = !studentAddExerciseFormVisible },
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
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Банк упражнений", fontWeight = FontWeight.Bold)
                AccessibleTextButton(
                    onClick = onBankExpandedToggle,
                    contentDescription = if (bankExpanded) {
                        UiStrings.BANK_SHOW_HIDE_A11Y_HIDE
                    } else {
                        UiStrings.BANK_SHOW_HIDE_A11Y_SHOW
                    }
                ) {
                    Text(if (bankExpanded) "Скрыть" else "Показать")
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
    item {
        Button(
            onClick = onExportTrainerWorkout,
            enabled = state.trainerWorkout.isNotEmpty(),
            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
        ) { Text("Сформировать тренировку") }
    }
    item {
        Text(
            "После нажатия JSON появится в блоке ниже — скопируйте и отправьте ученику.",
            fontWeight = FontWeight.Normal
        )
    }
    items(state.trainerWorkout) { item ->
        PlannedExerciseRow(item = item, onRemove = onRemoveWorkoutItem)
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
    item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onSelectSelfSection,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) { Text("Самостоятельная тренировка") }
            Button(
                onClick = onSelectTrainerSection,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Загрузить тренировку от тренера")
            }
        }
    }
    if (studentSelfSectionVisible) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Банк упражнений", fontWeight = FontWeight.Bold)
                    AccessibleTextButton(
                        onClick = onToggleAddExerciseForm,
                        contentDescription = UiStrings.ADD_EXERCISE_FORM_A11Y
                    ) {
                        Text(UiStrings.ADD_OWN_EXERCISE)
                    }
                }
            }
        }
        if (studentAddExerciseFormVisible) {
            item {
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
        item {
            Button(
                onClick = onStartSelfWorkout,
                enabled = state.selfWorkout.isNotEmpty(),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) { Text("Запустить самостоятельную тренировку") }
        }
        items(state.selfWorkout) { item ->
            PlannedExerciseRow(item = item, onRemove = onRemoveWorkoutItem)
        }
    }
    if (studentTrainerSectionVisible) {
        item {
            OutlinedTextField(
                value = trainerImportInput,
                onValueChange = onTrainerImportChange,
                label = { Text("JSON тренировки от тренера") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                onClick = { onImportTrainerWorkout(trainerImportInput) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) { Text("Импортировать") }
        }
        item {
            state.importStatus?.let { Text(it) }
        }
        items(state.trainerWorkout) { item ->
            PlannedExerciseRow(item = item, onRemove = onRemoveWorkoutItem)
        }
        item {
            Button(
                onClick = onStartTrainerWorkout,
                enabled = state.trainerWorkout.isNotEmpty(),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) { Text("Запустить тренировку от тренера") }
        }
    }
}

@Composable
private fun ExerciseBankEntryCard(
    exercise: Exercise,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(exercise.title, fontWeight = FontWeight.Medium)
            if (exercise.description.isNotBlank()) {
                Text(exercise.description)
            }
            Text("Повторения: ${exercise.defaultReps}")
            Button(
                onClick = onAction,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) { Text(actionLabel) }
        }
    }
}

@Composable
private fun PlannedExerciseRow(
    item: WorkoutExercise,
    onRemove: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${item.sortOrder + 1}. ${item.title} x ${item.plannedReps}")
            AccessibleTextButton(
                onClick = { onRemove(item.id) },
                contentDescription = UiStrings.REMOVE_FROM_LIST_A11Y
            ) { Text("Удалить") }
        }
    }
}

@Composable
private fun CopyableTrainerJsonBlock(
    exportedJson: String,
    importStatus: String?,
    clipboard: Clipboard,
    scope: CoroutineScope
) {
    if (exportedJson.isNotBlank()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(UiStrings.JSON_FOR_STUDENT, fontWeight = FontWeight.Bold)
                Text(exportedJson, style = MaterialTheme.typography.bodySmall)
                AccessibleTextButton(
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(
                                ClipEntry(ClipData.newPlainText("trainer_workout_json", exportedJson))
                            )
                        }
                    },
                    contentDescription = UiStrings.COPY_JSON_A11Y
                ) {
                    Text("Копировать JSON")
                }
            }
        }
    }
    importStatus?.let { Text(it) }
}

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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(title, fontWeight = FontWeight.Bold)
        }
        item {
            Text("Выполнено: $doneCount из $total", fontWeight = FontWeight.Bold)
        }
        item {
            LinearProgressIndicator(
                progress = {
                    if (total == 0) 0f else doneCount / total.toFloat()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (total == 0) {
            item { Text("На сегодня нет упражнений в тренировке.") }
            item {
                Button(
                    onClick = onFinish,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) { Text("Назад") }
            }
            return@LazyColumn
        }

        if (sessionFinished) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Тренировка завершена", fontWeight = FontWeight.Bold)
                        Text("Вы выполнили $doneCount из $total упражнений.")
                        Button(
                            onClick = onFinish,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        ) { Text("К списку тренировок") }
                    }
                }
            }
            return@LazyColumn
        }

        val current = items[currentIndex]
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Текущее упражнение", fontWeight = FontWeight.Bold)
                    Text(current.title)
                    Text("Обратный отсчет: $remainingSeconds сек")
                    Button(
                        onClick = { timerRunning = true },
                        enabled = !timerRunning,
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Запустить отсчет")
                    }
                }
            }
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
                    Exercise(
                        id = 1L,
                        title = "Приседания",
                        description = "3 подхода в комфортном темпе",
                        defaultReps = 15
                    ),
                    Exercise(
                        id = 2L,
                        title = "Планка",
                        description = "Удержание корпуса",
                        defaultReps = 45
                    )
                ),
                selfWorkout = listOf(
                    WorkoutExercise(
                        id = 10L,
                        dateIso = "2026-04-01",
                        exerciseId = 1L,
                        title = "Приседания",
                        plannedReps = 15,
                        sortOrder = 0
                    ),
                    WorkoutExercise(
                        id = 11L,
                        dateIso = "2026-04-01",
                        exerciseId = 2L,
                        title = "Планка",
                        plannedReps = 45,
                        sortOrder = 1
                    )
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
