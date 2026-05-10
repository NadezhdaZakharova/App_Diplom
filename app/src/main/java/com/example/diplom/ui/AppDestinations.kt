package com.example.diplom.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.diplom.R

enum class AppDestinations(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    @param:StringRes val contentDescriptionRes: Int,
) {
    TRAINING(R.string.nav_training_tab, Icons.Default.Build, R.string.nav_training_a11y),
    REWARDS(R.string.nav_rewards_tab, Icons.Default.Star, R.string.nav_rewards_a11y),
}
