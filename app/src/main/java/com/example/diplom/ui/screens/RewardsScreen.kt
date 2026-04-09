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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.domain.model.StudentRewardsStats
import com.example.diplom.ui.MainUiState
import com.example.diplom.ui.theme.DarkBackground
import com.example.diplom.ui.theme.DarkSurface
import com.example.diplom.ui.theme.GreenPrimary
import com.example.diplom.ui.theme.OrangeAccent
import com.example.diplom.ui.theme.TextPrimaryDark

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
        label = "monthProgress"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // 🔥 Заголовок + уровень
        item {
            Text(
                "Достижения",
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(6.dp))

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
            SectionHeader("Активность", Icons.Default.FitnessCenter)
            Text("Шагов: $totalSteps", color = TextPrimaryDark)
            Text("Дистанция: ${"%.2f".format(totalKm)} км", color = TextPrimaryDark)
        }

        items(state.recentDays) { day ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(0.5.dp, Color(0xFF1E2A38))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(day.dateIso, color = TextPrimaryDark)
                    Text("${day.steps} шагов", color = GreenPrimary)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(0.5.dp, Color(0xFF1E2A38))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun WeekTopExerciseContent(r: StudentRewardsStats) {
    Text(
        if (r.weekTopExerciseCount > 0 && r.weekTopExerciseTitle != null) {
            "Упражнение недели — ${r.weekTopExerciseTitle}"
        } else {
            "Упражнение недели — пока нет данных; завершите тренировки на этой неделе"
        },
        fontWeight = FontWeight.Medium
    )
    if (r.weekTopExerciseCount > 0) {
        Text("Сколько раз за неделю: ${r.weekTopExerciseCount}")
    }
}

@Composable
private fun WeeklyMarathonContent(r: StudentRewardsStats) {
    Text(
        if (r.weeklyMarathonComplete) {
            "Недельный марафон — вы тренировались каждый день недели (пн–вс)"
        } else {
            "Недельный марафон — тренируйтесь каждый день недели (пн–вс). Сейчас: ${r.workoutDaysThisWeek} из 7 дней"
        },
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun MonthlyWorkoutsContent(r: StudentRewardsStats, progress: Float) {
    val color by animateColorAsState(
        targetValue = if (progress > 0.7f) GreenPrimary else OrangeAccent,
        label = "progressColor"
    )

    Text("Тренировки в этом месяце", fontWeight = FontWeight.Bold, color = TextPrimaryDark)

    Text(
        "${r.workoutSessionsThisMonth} из ${r.daysInMonth}",
        color = color,
        fontWeight = FontWeight.SemiBold
    )

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = color,
        trackColor = Color(0xFF1E2A38)
    )
}
@Composable
private fun StreakContent(r: StudentRewardsStats) {
    Text("Серия дней с тренировками", fontWeight = FontWeight.Bold)
    Text("Текущая серия: ${r.currentStreakDays} дн.")
    Text(
        streakThresholdsLine(r),
        style = MaterialTheme.typography.bodySmall
    )
}

private fun streakThresholdsLine(r: StudentRewardsStats): String = buildString {
    append("Пороги: ")
    append(if (r.streakUnlocked3) "✓" else "○")
    append(" 3 дн.  ")
    append(if (r.streakUnlocked7) "✓" else "○")
    append(" 7 дн.  ")
    append(if (r.streakUnlocked14) "✓" else "○")
    append(" 14 дн.  ")
    append(if (r.streakUnlocked30) "✓" else "○")
    append(" 30 дн.")
}

@Composable
private fun FirstWeekContent(r: StudentRewardsStats) {
    Text("Первая неделя", fontWeight = FontWeight.Bold)
    Text(
        "Завершите не меньше ${r.firstWeekTarget} тренировок в первые 7 дней после установки приложения."
    )
    Text("Сейчас: ${r.firstWeekWorkoutsCount} / ${r.firstWeekTarget}")
    Text(
        if (r.firstWeekComplete) "Цель достигнута" else "Продолжайте тренироваться",
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun VarietyContent(r: StudentRewardsStats) {
    Text("Разнообразие", fontWeight = FontWeight.Bold)
    Text(
        "За текущую неделю выполните упражнения из не меньше ${r.varietyTarget} разных позиций банка."
    )
    Text("Разных упражнений на неделе: ${r.varietyDistinctExercises} / ${r.varietyTarget}")
    Text(
        if (r.varietyComplete) "Цель достигнута" else "Добавьте разные упражнения в тренировки",
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun TrainerWorkoutContent(r: StudentRewardsStats) {
    Text("Первая тренировка от тренера", fontWeight = FontWeight.Bold)
    Text(
        if (r.completedTrainerWorkout) {
            "Вы завершили тренировку по плану тренера"
        } else {
            "Импортируйте JSON от тренера и завершите такую тренировку"
        },
        fontWeight = FontWeight.Medium
    )
}
