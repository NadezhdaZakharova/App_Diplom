package com.example.diplom.domain.model

data class WeeklyChallenge(
    val weekStartIso: String,
    val targetSteps: Int,
    val progressSteps: Int,
    val completed: Boolean
)
