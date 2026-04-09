package com.example.diplom.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.ui.MainUiState

@Composable
fun RewardsAndStatsScreen(state: MainUiState, modifier: Modifier = Modifier) {

    val totalSteps = state.recentDays.sumOf { it.steps }
    val totalKm = state.recentDays.sumOf { it.distanceKm }
    val r = state.studentRewards

    val monthProgress =
        if (r.daysInMonth > 0) {
            (r.workoutSessionsThisMonth / r.daysInMonth.toFloat()).coerceIn(0f, 1f)
        } else 0f

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

        // 🔥 HEADER
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(
                        "🔥 Твой прогресс",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Ты на пути к лучшей форме 💪",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 🏆 достижения
        item { AchievementCard { WeekTopExerciseContent(r) } }
        item { AchievementCard { WeeklyMarathonContent(r) } }
        item { AchievementCard { MonthlyWorkoutsContent(r, progressAnim) } }
        item { AchievementCard { StreakContent(r) } }
        item { AchievementCard { FirstWeekContent(r) } }
        item { AchievementCard { VarietyContent(r) } }
        item { AchievementCard { TrainerWorkoutContent(r) } }

        // 📊 активность
        item {
            Text(
                "📊 Активность",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                "Шагов: $totalSteps",
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                "Дистанция: ${"%.2f".format(totalKm)} км",
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // 📅 дни
        items(state.recentDays) { day ->

            val progress = (day.steps / 10000f).coerceIn(0f, 1f)

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
                            day.dateIso,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${day.steps} шагов",
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
    Text("🏆 Упражнение недели", fontWeight = FontWeight.Bold)

    if (r.weekTopExerciseCount > 0 && r.weekTopExerciseTitle != null) {
        Text("${r.weekTopExerciseTitle}")
        Text("Повторений: ${r.weekTopExerciseCount}")
    } else {
        Text("Пока нет данных — начни тренироваться 💪")
    }
}

@Composable
private fun WeeklyMarathonContent(r: StudentRewardsStats) {
    Text("🔥 Недельный марафон", fontWeight = FontWeight.Bold)

    Text(
        if (r.weeklyMarathonComplete) {
            "Ты тренировалась каждый день недели 🎉"
        } else {
            "Дни тренировок: ${r.workoutDaysThisWeek} / 7"
        }
    )
}

@Composable
private fun MonthlyWorkoutsContent(r: StudentRewardsStats, progress: Float) {

    val color by animateColorAsState(
        targetValue = if (progress > 0.7f)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.secondary,
        label = "color"
    )

    Text("📅 Тренировки в месяце", fontWeight = FontWeight.Bold)

    Text("${r.workoutSessionsThisMonth} / ${r.daysInMonth}")

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
    Text("🔥 Серия тренировок", fontWeight = FontWeight.Bold)

    Text("Текущая серия: ${r.currentStreakDays} дней")

    Text(
        buildString {
            append("Пороги: ")

            append(if (r.streakUnlocked3) "✅" else "⬜")
            append(" 3  ")

            append(if (r.streakUnlocked7) "✅" else "⬜")
            append(" 7  ")

            append(if (r.streakUnlocked14) "✅" else "⬜")
            append(" 14  ")

            append(if (r.streakUnlocked30) "✅" else "⬜")
            append(" 30")
        },
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun FirstWeekContent(r: StudentRewardsStats) {
    Text("🚀 Первая неделя", fontWeight = FontWeight.Bold)

    Text(
        "Сделай минимум ${r.firstWeekTarget} тренировок за первые 7 дней"
    )

    Text(
        "Прогресс: ${r.firstWeekWorkoutsCount} / ${r.firstWeekTarget}"
    )

    Text(
        if (r.firstWeekComplete) "Цель достигнута 🎉"
        else "Продолжай 💪",
        color = if (r.firstWeekComplete)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun VarietyContent(r: StudentRewardsStats) {
    Text("🎯 Разнообразие", fontWeight = FontWeight.Bold)

    Text(
        "Сделай ${r.varietyTarget} разных упражнений за неделю"
    )

    Text(
        "Прогресс: ${r.varietyDistinctExercises} / ${r.varietyTarget}"
    )

    Text(
        if (r.varietyComplete) "Выполнено ✅"
        else "Добавь новые упражнения",
        color = if (r.varietyComplete)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun TrainerWorkoutContent(r: StudentRewardsStats) {
    Text("💪 Первая тренировка от тренера", fontWeight = FontWeight.Bold)

    Text(
        if (r.completedTrainerWorkout) {
            "Ты завершила тренировку по плану тренера ✅"
        } else {
            "Импортируй тренировку от тренера и выполни её"
        },
        color = if (r.completedTrainerWorkout)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface
    )
}