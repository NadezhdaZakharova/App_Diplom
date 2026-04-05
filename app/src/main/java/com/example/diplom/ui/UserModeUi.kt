package com.example.diplom.ui

import com.example.diplom.domain.model.AppUserMode

fun AppUserMode.roleLabel(): String = when (this) {
    AppUserMode.STUDENT -> UiStrings.ROLE_STUDENT
    AppUserMode.TRAINER -> UiStrings.ROLE_TRAINER
}

fun AppUserMode.topBarTitle(): String = when (this) {
    AppUserMode.STUDENT -> UiStrings.MODE_TITLE_STUDENT
    AppUserMode.TRAINER -> UiStrings.MODE_TITLE_TRAINER
}

fun AppUserMode.toggle(): AppUserMode = when (this) {
    AppUserMode.STUDENT -> AppUserMode.TRAINER
    AppUserMode.TRAINER -> AppUserMode.STUDENT
}
