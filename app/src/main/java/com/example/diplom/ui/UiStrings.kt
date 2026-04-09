package com.example.diplom.ui

/**
 * Единая терминология в интерфейсе: «ученик» и «тренер».
 */
object UiStrings {
    const val ROLE_STUDENT = "🎓 Ученик"
    const val ROLE_TRAINER = "🏋️ Тренер"

    const val MODE_TITLE_STUDENT = "🎓 Режим ученика"
    const val MODE_TITLE_TRAINER = "🏋️ Режим тренера"

    const val PICK_MODE_TITLE = "Выберите режим"
    const val PICK_MODE_HEADING_A11Y =
        "Выберите режим работы: ученик или тренер"

    const val SWITCH_MODE = "🔄 Сменить режим"
    fun switchModeToA11y(targetRole: String) = "Переключить на режим $targetRole"

    const val NAV_TRAINING_TAB = "💪 Тренировка"
    const val NAV_REWARDS_TAB = "🏆 Награды"
    const val NAV_TRAINING_A11Y = "Вкладка «Тренировка»: план и запуск тренировок"
    const val NAV_REWARDS_A11Y = "Вкладка «Награды»: достижения и активность"

    const val SELECT_STUDENT_A11Y =
        "Режим ученика: план упражнений и самостоятельные тренировки"
    const val SELECT_TRAINER_A11Y =
        "Режим тренера: банк упражнений и программа для ученика"

    const val TRAINER_PLAN_HEADING = "📋 План для ученика"
    const val STUDENT_TODAY_HEADING = "🔥 Тренировка на сегодня"
    const val ACTION_FOR_STUDENT = "Для ученика"
    const val ACTION_TO_WORKOUT = "В тренировку"
    const val JSON_FOR_STUDENT = "JSON для ученика"
    const val ADD_OWN_EXERCISE = "➕ Добавить упражнение"

    const val COPY_JSON_A11Y = "Скопировать JSON тренировки в буфер обмена"
    const val REMOVE_FROM_LIST_A11Y = "Удалить упражнение из списка тренировки"
    const val BANK_SHOW_HIDE_A11Y_SHOW = "Показать банк упражнений"
    const val BANK_SHOW_HIDE_A11Y_HIDE = "Скрыть банк упражнений"
    const val ADD_EXERCISE_FORM_A11Y = "Показать или скрыть форму добавления своего упражнения"
}
