package com.example.diplom.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.diplom.R
import com.example.diplom.domain.ExerciseTitleKeys

@Composable
fun exerciseLocalizedTitle(titleKey: String?, fallback: String): String {
    if (titleKey.isNullOrBlank()) return fallback
    val resId = exerciseTitleStringRes(titleKey) ?: return fallback
    return stringResource(resId)
}

private fun exerciseTitleStringRes(titleKey: String): Int? = when (titleKey) {
    ExerciseTitleKeys.PRESET_SQUATS -> R.string.exercise_preset_squats
    ExerciseTitleKeys.PRESET_PUSHUPS -> R.string.exercise_preset_pushups
    ExerciseTitleKeys.PRESET_PLANK -> R.string.exercise_preset_plank
    else -> null
}
