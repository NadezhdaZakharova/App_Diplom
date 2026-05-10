package com.example.diplom.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.ui.MainUiState
import com.example.diplom.ui.exerciseLocalizedTitle

@Composable
fun RewardsAndStatsScreen(
    state: MainUiState,
    modifier: Modifier = Modifier,
    onConfirmStepsConversion: () -> Unit = {},
    onDismissStepsConversion: () -> Unit = {}
) {

    val todaySteps = state.today.steps
    val todayKm = state.today.distanceKm
    val r = state.studentRewards
    val stepGoal = state.dailyGoal.coerceAtLeast(1)

    val monthProgress =
        if (r.daysInMonth > 0) {
            (r.workoutSessionsThisMonth / r.daysInMonth.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val progressAnim by animateFloatAsState(
        targetValue = monthProgress,
        animationSpec = tween(800),
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
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        text = stringResource(R.string.rewards_your_progress_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.rewards_on_track_subtitle),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        item { AchievementCard { WeekTopExerciseContent(r) } }
        item { AchievementCard { WeeklyMarathonContent(r) } }
        item { AchievementCard { MonthlyWorkoutsContent(r, progressAnim) } }
        item { AchievementCard { StreakContent(r) } }
        item { AchievementCard { FirstWeekContent(r) } }
        item { AchievementCard { VarietyContent(r) } }
        item { AchievementCard { TrainerWorkoutContent(r) } }

        item {
            val dailyGoalMet =
                state.dailyGoal > 0 && state.today.steps >= state.dailyGoal
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.showStepsToWorkoutConversion) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.steps_conversion_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.steps_conversion_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = onDismissStepsConversion) {
                                    Text(stringResource(R.string.steps_conversion_no))
                                }
                                TextButton(onClick = onConfirmStepsConversion) {
                                    Text(stringResource(R.string.steps_conversion_yes))
                                }
                            }
                        }
                    }
                }

                if (dailyGoalMet) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFC8E6C9)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF43A047))
                    ) {
                        Text(
                            text = stringResource(R.string.rewards_step_goal_met_banner),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.rewards_activity_heading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = stringResource(R.string.rewards_steps_line, todaySteps),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = stringResource(R.string.rewards_distance_km, todayKm),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.rewards_steps_streak_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(state.recentDays) { day ->

            val progress = (day.steps / stepGoal.toFloat()).coerceIn(0f, 1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = day.dateIso,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.rewards_day_steps_count, day.steps),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun WeekTopExerciseContent(r: StudentRewardsStats) {
    Text(
        text = stringResource(R.string.rewards_week_exercise_heading),
        fontWeight = FontWeight.Bold
    )

    val hasWeekTop = r.weekTopExerciseCount > 0 &&
        (r.weekTopExerciseTitleKey != null || !r.weekTopExerciseTitleFallback.isNullOrBlank())
    if (hasWeekTop) {
        Text(
            text = exerciseLocalizedTitle(
                r.weekTopExerciseTitleKey,
                r.weekTopExerciseTitleFallback.orEmpty()
            )
        )
        Text(
            text = stringResource(
                R.string.rewards_week_top_label_with_count,
                stringResource(R.string.rewards_week_top_label),
                r.weekTopExerciseCount
            )
        )
    } else {
        Text(text = stringResource(R.string.rewards_week_top_empty))
    }
}

@Composable
private fun WeeklyMarathonContent(r: StudentRewardsStats) {
    Text(
        text = stringResource(R.string.rewards_weekly_marathon_heading),
        fontWeight = FontWeight.Bold
    )

    Text(
        text = if (r.weeklyMarathonComplete) {
            stringResource(R.string.rewards_weekly_marathon_complete)
        } else {
            stringResource(R.string.rewards_weekly_marathon_progress, r.workoutDaysThisWeek)
        }
    )
}

@Composable
private fun MonthlyWorkoutsContent(r: StudentRewardsStats, progress: Float) {

    val color by animateColorAsState(
        targetValue = if (progress > 0.7f) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondary
        },
        label = "color"
    )

    Text(
        text = stringResource(R.string.rewards_monthly_heading),
        fontWeight = FontWeight.Bold
    )

    Text(
        text = stringResource(
            R.string.rewards_monthly_count,
            r.workoutSessionsThisMonth,
            r.daysInMonth
        )
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(10.dp)),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
private fun StreakContent(r: StudentRewardsStats) {
    val cellDone = stringResource(R.string.rewards_streak_cell_done)
    val cellTodo = stringResource(R.string.rewards_streak_cell_todo)

    Text(
        text = stringResource(R.string.rewards_streak_heading),
        fontWeight = FontWeight.Bold
    )

    Text(text = stringResource(R.string.rewards_streak_current, r.currentStreakDays))

    Text(
        text = stringResource(
            R.string.rewards_streak_thresholds,
            if (r.streakUnlocked3) cellDone else cellTodo,
            if (r.streakUnlocked7) cellDone else cellTodo,
            if (r.streakUnlocked14) cellDone else cellTodo,
            if (r.streakUnlocked30) cellDone else cellTodo
        ),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun FirstWeekContent(r: StudentRewardsStats) {
    Text(
        text = stringResource(R.string.rewards_first_week_heading),
        fontWeight = FontWeight.Bold
    )

    Text(text = stringResource(R.string.rewards_first_week_goal, r.firstWeekTarget))

    Text(
        text = stringResource(
            R.string.rewards_first_week_progress,
            r.firstWeekWorkoutsCount,
            r.firstWeekTarget
        )
    )

    Text(
        text = if (r.firstWeekComplete) {
            stringResource(R.string.rewards_first_week_done)
        } else {
            stringResource(R.string.rewards_first_week_continue)
        },
        color = if (r.firstWeekComplete) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}

@Composable
private fun VarietyContent(r: StudentRewardsStats) {
    Text(
        text = stringResource(R.string.rewards_variety_heading),
        fontWeight = FontWeight.Bold
    )

    Text(text = stringResource(R.string.rewards_variety_goal, r.varietyTarget))

    Text(
        text = stringResource(
            R.string.rewards_variety_progress,
            r.varietyDistinctExercises,
            r.varietyTarget
        )
    )

    Text(
        text = if (r.varietyComplete) {
            stringResource(R.string.rewards_variety_done)
        } else {
            stringResource(R.string.rewards_variety_hint)
        },
        color = if (r.varietyComplete) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}

@Composable
private fun TrainerWorkoutContent(r: StudentRewardsStats) {
    Text(
        text = stringResource(R.string.rewards_trainer_first_heading),
        fontWeight = FontWeight.Bold
    )

    Text(
        text = if (r.completedTrainerWorkout) {
            stringResource(R.string.rewards_trainer_done)
        } else {
            stringResource(R.string.rewards_trainer_todo)
        },
        color = if (r.completedTrainerWorkout) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )
}
