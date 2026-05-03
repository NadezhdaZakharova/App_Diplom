package com.example.diplom.domain.model

data class DailyStats(
    val dateIso: String,
    val steps: Int,
    val activeMinutes: Int,
    val distanceKm: Double
)
