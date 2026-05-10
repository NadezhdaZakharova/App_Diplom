package com.example.diplom.domain.usecase

import com.example.diplom.domain.model.WorkoutExercise
import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class RecordCompletedStudentWorkoutUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(items: List<WorkoutExercise>, fromTrainerPlan: Boolean) {
        trainingRepository.recordCompletedStudentWorkout(items, fromTrainerPlan)
    }
}
