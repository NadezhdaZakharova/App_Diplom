package com.example.diplom.domain.model

/** Дневная активность по шагам (доменная модель без привязки к Room). */
data class DailyStepRecord(
    val dateIso: String,
    val steps: Int,
    val activeMinutes: Int
)
