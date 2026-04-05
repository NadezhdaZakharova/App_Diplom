package com.example.diplom.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
) {
    TRAINING(UiStrings.NAV_TRAINING_TAB, Icons.Default.Build, UiStrings.NAV_TRAINING_A11Y),
    REWARDS(UiStrings.NAV_REWARDS_TAB, Icons.Default.Star, UiStrings.NAV_REWARDS_A11Y),
}
