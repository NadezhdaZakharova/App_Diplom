package com.example.diplom.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R
import com.example.diplom.ui.exerciseLocalizedTitle
import com.example.diplom.domain.model.WorkoutExercise
import kotlinx.coroutines.delay

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
                            stringResource(R.string.workout_progress_format, doneCount, total),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            stringResource(
                                R.string.workout_progress_percent,
                                if (total == 0) 0 else (doneCount * 100 / total)
                            ),
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
                Text(
                    stringResource(R.string.workout_empty_list),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            item {
                OutlineButton(
                    stringResource(R.string.back),
                    onFinish,
                    modifier = Modifier.fillMaxWidth()
                )
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
                            stringResource(R.string.workout_completed_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp
                        )
                        Text(
                            stringResource(R.string.workout_completed_count, doneCount, total),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        GreenButton(
                            stringResource(R.string.back_to_workouts),
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
                        stringResource(R.string.current_exercise),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        exerciseLocalizedTitle(current.titleKey, current.title),
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
                            contentDescription = stringResource(R.string.a11y_timer_icon),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        val timerColor by animateColorAsState(
                            targetValue = if (remainingSeconds <= 5) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            animationSpec = tween(500),
                            label = "timerColor"
                        )
                        Text(
                            stringResource(R.string.timer_seconds_format, remainingSeconds),
                            color = timerColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    GreenButton(
                        text = if (timerRunning) {
                            stringResource(R.string.pause)
                        } else {
                            stringResource(R.string.start_timer)
                        },
                        onClick = { timerRunning = !timerRunning },
                        icon = if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlineButton(
                        text = stringResource(R.string.close_workout_mode),
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
            title = { Text(stringResource(R.string.exit_workout_title)) },
            text = { Text(stringResource(R.string.exit_workout_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmation = false
                        onFinish()
                    }
                ) {
                    Text(stringResource(R.string.exit_confirm))
                }
            },
            dismissButton = {
                OutlineButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showExitConfirmation = false }
                )
            }
        )
    }
}
