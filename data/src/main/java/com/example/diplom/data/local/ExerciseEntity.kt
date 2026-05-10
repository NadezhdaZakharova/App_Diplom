package com.example.diplom.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_bank")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val defaultReps: Int,
    val titleKey: String? = null
)
