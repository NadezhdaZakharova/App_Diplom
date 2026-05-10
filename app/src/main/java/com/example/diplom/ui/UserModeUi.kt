package com.example.diplom.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.example.diplom.R
import com.example.diplom.domain.model.AppUserMode

@Composable
@ReadOnlyComposable
fun AppUserMode.roleLabel(): String = when (this) {
    AppUserMode.STUDENT -> stringResource(R.string.role_student)
    AppUserMode.TRAINER -> stringResource(R.string.role_trainer)
}

@Composable
@ReadOnlyComposable
fun AppUserMode.topBarTitle(): String = when (this) {
    AppUserMode.STUDENT -> stringResource(R.string.mode_title_student)
    AppUserMode.TRAINER -> stringResource(R.string.mode_title_trainer)
}

fun AppUserMode.toggle(): AppUserMode = when (this) {
    AppUserMode.STUDENT -> AppUserMode.TRAINER
    AppUserMode.TRAINER -> AppUserMode.STUDENT
}
