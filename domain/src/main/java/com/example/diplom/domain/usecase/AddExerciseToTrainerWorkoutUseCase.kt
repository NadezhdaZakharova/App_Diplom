package com.example.diplom.domain.usecase

import com.example.diplom.domain.model.Exercise
import com.example.diplom.domain.repository.TrainingRepository
import javax.inject.Inject

class AddExerciseToTrainerWorkoutUseCase @Inject constructor(
    private val trainingRepository: TrainingRepository
) {
    suspend operator fun invoke(exercise: Exercise) {
        trainingRepository.addExerciseToTrainerWorkout(exercise)
    }
}
