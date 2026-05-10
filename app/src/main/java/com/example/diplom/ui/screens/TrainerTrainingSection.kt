package com.example.diplom.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R
import com.example.diplom.domain.model.Exercise
import com.example.diplom.ui.MainUiState
import com.example.diplom.ui.components.AccessibleTextButton
import kotlinx.coroutines.CoroutineScope

@Suppress("LongParameterList")
internal fun LazyListScope.trainerTrainingContent(
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
        val bankHideA11y = stringResource(R.string.bank_hide_a11y)
        val bankShowA11y = stringResource(R.string.bank_show_a11y)
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
                Text(
                    stringResource(R.string.exercise_bank_title),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                AccessibleTextButton(
                    onClick = onBankExpandedToggle,
                    contentDescription = if (bankExpanded) bankHideA11y else bankShowA11y
                ) {
                    Text(
                        if (bankExpanded) stringResource(R.string.hide) else stringResource(R.string.show),
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
                actionLabel = stringResource(R.string.action_for_student),
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
            text = stringResource(R.string.build_trainer_workout),
            onClick = onExportTrainerWorkout,
            enabled = state.trainerWorkout.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        )
    }

    item {
        Text(
            stringResource(R.string.trainer_json_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }

    item {
        CopyableTrainerJsonBlock(
            exportedJson = state.exportedJson,
            clipboard = clipboard,
            scope = scope,
            onShareJson = onShareJson
        )
    }
}
