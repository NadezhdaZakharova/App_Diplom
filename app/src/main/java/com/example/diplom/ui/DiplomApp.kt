package com.example.diplom.ui
import com.example.diplom.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diplom.ui.components.AccessibleTextButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplom.domain.model.AppUserMode
import com.example.diplom.ui.screens.RewardsAndStatsScreen
import com.example.diplom.ui.screens.TrainingScreen
import com.example.diplom.ui.screens.WorkoutSessionScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiplomApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val nav by viewModel.appNavigation.collectAsState()

    val visibleDestinations = remember(uiState.userMode) {
        buildList {
            add(AppDestinations.TRAINING)
            if (uiState.userMode == AppUserMode.STUDENT) add(AppDestinations.REWARDS)
        }
    }

    LaunchedEffect(uiState.userMode) {
        viewModel.syncMainDestinationWithUserMode(uiState.userMode)
    }

    when {
        nav.showModePicker && !nav.sessionActive -> ModeSelectionScreen(
            onTrainer = {
                viewModel.setUserMode(AppUserMode.TRAINER)
                viewModel.dismissModePicker()
            },
            onStudent = {
                viewModel.setUserMode(AppUserMode.STUDENT)
                viewModel.dismissModePicker()
            }
        )
        nav.sessionActive -> Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            WorkoutSessionScreen(
                sessionInstanceId = nav.sessionInstanceId,
                title = nav.sessionTitle,
                items = nav.sessionItems,
                modifier = Modifier.padding(innerPadding),
                onFinish = { viewModel.endWorkoutSession() },
                onWorkoutCompleted = { completed ->
                    if (uiState.userMode == AppUserMode.STUDENT) {
                        viewModel.recordStudentWorkoutCompletion(completed, nav.sessionFromTrainer)
                    }
                }
            )
        }
        else -> MainScaffold(
            uiState = uiState,
            nav = nav,
            visibleDestinations = visibleDestinations,
            viewModel = viewModel
        )
    }
}

@Composable
private fun ModeSelectionScreen(
    onTrainer: () -> Unit,
    onStudent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = stringResource(R.string.pick_mode_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // УЧЕНИК
        ModeCard(
            title = stringResource(R.string.role_student),
            description = stringResource(R.string.mode_student_description),
            imageRes = R.drawable.student,
            onClick = onStudent
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ТРЕНЕР
        ModeCard(
            title = stringResource(R.string.role_trainer),
            description = stringResource(R.string.mode_trainer_description),
            imageRes = R.drawable.trainer,
            onClick = onTrainer
        )
    }
}
@Composable
fun ModeCard(
    title: String,
    description: String,
    imageRes: Int,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // КАРТИНКА
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // ТЕКСТ
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    uiState: MainUiState,
    nav: DiplomAppNavigationState,
    visibleDestinations: List<AppDestinations>,
    viewModel: MainViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(uiState.importNotificationToken) {
        val notification = uiState.importNotification ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = notification.toImportTransferMessage(context),
            duration = SnackbarDuration.Long
        )
    }

    PostNotificationsPermissionEffect()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.userMode.topBarTitle(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        AccessibleTextButton(
                            onClick = { viewModel.setUserMode(uiState.userMode.toggle()) },
                            contentDescription = stringResource(
                                R.string.switch_mode_a11y,
                                uiState.userMode.toggle().roleLabel()
                            )
                        ) {
                            Text(
                                stringResource(R.string.switch_mode),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        CompositionLocalProvider(LocalAppSnackbarHostState provides snackbarHostState) {
            NavigationSuiteScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                navigationSuiteItems = {
                    visibleDestinations.forEach { dest ->
                        item(
                            icon = {
                                Icon(
                                    dest.icon,
                                    contentDescription = stringResource(dest.contentDescriptionRes)
                                )
                            },
                            label = { Text(stringResource(dest.labelRes)) },
                            selected = dest == nav.currentDestination,
                            onClick = { viewModel.setMainDestination(dest) }
                        )
                    }
                }
            ) {
                when (nav.currentDestination) {
                    AppDestinations.TRAINING -> TrainingScreen(
                        state = uiState,
                        modifier = Modifier.fillMaxSize(),
                        onAddExercise = viewModel::addExercise,
                        onUpdateExercise = viewModel::updateExercise,
                        onConsumedOpenStudentTrainerSection = viewModel::consumeOpenStudentTrainerSectionRequest,
                        onAddToWorkout = viewModel::addToWorkout,
                        onAddToTrainerWorkout = viewModel::addToTrainerWorkout,
                        onSaveTrainerExerciseToBank = { item ->
                            viewModel.addExercise(
                                title = item.title,
                                description = item.description,
                                defaultDurationSeconds = item.plannedReps
                            )
                        },
                        onRemoveWorkoutItem = viewModel::removeWorkoutItem,
                        onMoveWorkoutItem = viewModel::moveWorkoutItem,
                        onImportTrainerWorkout = viewModel::importTrainerWorkout,
                        onExportTrainerWorkout = viewModel::exportTrainerWorkout,
                        onStartSelfWorkout = {
                            viewModel.startSelfWorkoutSession(uiState.selfWorkout)
                        },
                        onStartTrainerWorkout = {
                            viewModel.startTrainerWorkoutSession(uiState.trainerWorkout)
                        },
                        onSetDailyGoal = viewModel::setDailyGoal
                    )
                    AppDestinations.REWARDS -> RewardsAndStatsScreen(
                        state = uiState,
                        modifier = Modifier.fillMaxSize(),
                        onConfirmStepsConversion = viewModel::confirmStepsToWorkoutConversion,
                        onDismissStepsConversion = viewModel::declineStepsToWorkoutConversion
                    )
                }
            }
        }
    }
}
