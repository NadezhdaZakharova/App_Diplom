package com.example.diplom

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.diplom.core.DateUtils
import com.example.diplom.data.local.DiplomDatabase
import com.example.diplom.data.repository.TrainerWorkoutJsonStore
import com.example.diplom.data.repository.TrainerWorkoutPlannedSecondsParser
import com.example.diplom.data.repository.TrainingRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeInstrumentedTest {

    private val composeRule = createAndroidComposeRule<MainActivity>()

    private val grantPermissionRule: GrantPermissionRule =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                GrantPermissionRule.grant(
                    Manifest.permission.ACTIVITY_RECOGNITION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                GrantPermissionRule.grant(Manifest.permission.ACTIVITY_RECOGNITION)
            else -> GrantPermissionRule.grant()
        }

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(grantPermissionRule).around(composeRule)

    @Before
    fun clearTrainerPlanForToday() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val db = DiplomDatabase.getInstance(ctx)
        val today = DateUtils.todayIso()
        db.dao().clearPlannedWorkoutByTypeAndDate(today, "TRAINER")
    }

    @Test
    fun importTrainerJson_persistsToRoom() = runBlocking {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val db = DiplomDatabase.getInstance(ctx)
        val parser = TrainerWorkoutPlannedSecondsParser()
        val jsonStore = TrainerWorkoutJsonStore(parser)
        val repo = TrainingRepositoryImpl(db.dao(), jsonStore)
        val json =
            """{"schemaVersion":1,"trainerWorkout":[{"title":"SmokeTest Exercise","plannedSeconds":42,"sortOrder":0}]}"""
        val result = repo.importTrainerWorkoutFromJson(json)
        assertTrue(result.isSuccess)
        val today = DateUtils.todayIso()
        val trainerRows = db.dao().getPlannedWorkoutAll()
            .filter { it.dateIso == today && it.workoutType == "TRAINER" }
        assertEquals(1, trainerRows.size)
        assertEquals("SmokeTest Exercise", trainerRows.first().title)
        assertEquals(42, trainerRows.first().plannedReps)
    }

    @Test
    fun navigation_afterChoosingStudent_showsRewardsTab() {
        val studentTitle = composeRule.activity.getString(R.string.role_student)
        composeRule.onNodeWithText(studentTitle, substring = true).performClick()
        composeRule.waitForIdle()
        val rewardsA11y = composeRule.activity.getString(R.string.nav_rewards_a11y)
        composeRule.onNodeWithContentDescription(rewardsA11y).assertIsDisplayed()
    }
}
