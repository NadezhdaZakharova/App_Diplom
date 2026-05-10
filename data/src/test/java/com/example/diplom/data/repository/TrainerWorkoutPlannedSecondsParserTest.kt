package com.example.diplom.data.repository

import com.example.diplom.domain.TrainingConstraints
import org.json.JSONObject
import kotlin.test.assertFailsWith
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainerWorkoutPlannedSecondsParserTest {

    private val parser = TrainerWorkoutPlannedSecondsParser()

    @Test
    fun parsesPlannedSecondsNumber() {
        val item = JSONObject().put("plannedSeconds", 42)
        assertEquals(42, parser.parse(item, 1))
    }

    @Test
    fun parsesLegacyPlannedReps() {
        val item = JSONObject().put("plannedReps", 30)
        assertEquals(30, parser.parse(item, 1))
    }

    @Test
    fun plannedSecondsTakesPrecedenceOverReps() {
        val item = JSONObject().put("plannedSeconds", 5).put("plannedReps", 99)
        assertEquals(5, parser.parse(item, 1))
    }

    @Test
    fun parsesStringInt() {
        val item = JSONObject().put("plannedSeconds", "  60 ")
        assertEquals(60, parser.parse(item, 1))
    }

    @Test
    fun rejectsMissingField() {
        val item = JSONObject().put("title", "x")
        assertFailsWith<IllegalArgumentException> { parser.parse(item, 3) }
    }

    @Test
    fun rejectsZero() {
        val item = JSONObject().put("plannedSeconds", 0)
        assertFailsWith<IllegalArgumentException> { parser.parse(item, 1) }
    }

    @Test
    fun rejectsNegative() {
        val item = JSONObject().put("plannedSeconds", -1)
        assertFailsWith<IllegalArgumentException> { parser.parse(item, 1) }
    }

    @Test
    fun rejectsNonIntegerDouble() {
        val item = JSONObject().put("plannedSeconds", 3.5)
        assertFailsWith<IllegalArgumentException> { parser.parse(item, 1) }
    }

    @Test
    fun rejectsAboveMax() {
        val item = JSONObject().put("plannedSeconds", TrainingConstraints.MAX_EXERCISE_DURATION_SECONDS + 1)
        assertFailsWith<IllegalArgumentException> { parser.parse(item, 1) }
    }
}
