package com.example.diplom.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val unlockedAtIso: String?
)
