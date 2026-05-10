package com.example.diplom.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.diplom.R

internal fun parsedDurationSecondsOrNull(raw: String): Int? =
    raw.toIntOrNull()?.takeIf { it > 0 }

@Composable
internal fun defaultDurationFieldErrorOrNull(raw: String): String? = when {
    raw.isBlank() -> stringResource(R.string.duration_blank_error)
    raw.toIntOrNull()?.let { it > 0 } == true -> null
    else -> stringResource(R.string.duration_invalid_error)
}
