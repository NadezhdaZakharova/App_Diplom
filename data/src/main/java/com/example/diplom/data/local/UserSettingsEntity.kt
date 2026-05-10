package com.example.diplom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.diplom.domain.DEFAULT_DAILY_GOAL_STEPS

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val dailyGoal: Int = DEFAULT_DAILY_GOAL_STEPS,
    val mode: String = "STUDENT",
    val firstInstallDateIso: String = "",
    /** Дата (ISO), в которую пользователь отклонил предложение «шаги → тренировка»; сбрасывается сменой календарного дня в логике UI. */
    val stepsToWorkoutOfferDeclinedDateIso: String = ""
)
