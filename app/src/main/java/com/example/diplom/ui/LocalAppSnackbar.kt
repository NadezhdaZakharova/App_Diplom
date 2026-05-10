package com.example.diplom.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/** [SnackbarHostState] из [MainScaffold] — для сообщений из вложенных экранов (копирование JSON и т.д.). */
val LocalAppSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalAppSnackbarHostState: используйте только внутри MainScaffold")
}
