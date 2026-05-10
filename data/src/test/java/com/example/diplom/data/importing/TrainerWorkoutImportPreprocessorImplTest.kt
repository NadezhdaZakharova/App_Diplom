package com.example.diplom.data.importing

import org.junit.Assert.assertTrue
import org.junit.Test

class TrainerWorkoutImportPreprocessorImplTest {

    private val preprocessor = TrainerWorkoutImportPreprocessorImpl()

    @Test
    fun emptyInputFails() {
        val r = preprocessor.preparePayload("   ")
        assertTrue(r.isFailure)
    }

    @Test
    fun validTrainerWorkoutJsonSucceeds() {
        val json = """{"trainerWorkout":[{"title":"A","plannedSeconds":10}]}"""
        val r = preprocessor.preparePayload(json)
        assertTrue(r.isSuccess)
    }

    @Test
    fun missingArrayFails() {
        val r = preprocessor.preparePayload("""{"schemaVersion":1}""")
        assertTrue(r.isFailure)
    }
}
