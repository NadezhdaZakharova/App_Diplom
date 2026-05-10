package com.example.diplom.data.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.diplom.data.R
import com.example.diplom.domain.StepMilestoneNotifier
import com.example.diplom.domain.stepsMeetWorkoutStreakAlternative
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidStepMilestoneNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context
) : StepMilestoneNotifier {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun onStepTotalsUpdated(todaySteps: Int, dailyGoal: Int, todayIso: String) {
        if (dailyGoal <= 0) return
        if (!canPostNotifications()) return
        ensureChannel()

        val nm = NotificationManagerCompat.from(context)

        val lastGoal = prefs.getString(KEY_GOAL_DATE, "") ?: ""
        if (todaySteps >= dailyGoal && lastGoal != todayIso) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_step_daily_goal_title))
                .setContentText(context.getString(R.string.notification_step_daily_goal_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            if (safeNotify(nm, NOTIF_ID_DAILY_GOAL, notification)) {
                prefs.edit().putString(KEY_GOAL_DATE, todayIso).apply()
            }
        }

        val lastBonus = prefs.getString(KEY_BONUS_DATE, "") ?: ""
        if (stepsMeetWorkoutStreakAlternative(todaySteps, dailyGoal) && lastBonus != todayIso) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_step_bonus_title))
                .setContentText(context.getString(R.string.notification_step_bonus_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            if (safeNotify(nm, NOTIF_ID_BONUS, notification)) {
                prefs.edit().putString(KEY_BONUS_DATE, todayIso).apply()
            }
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_step_milestones_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_step_milestones_desc)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * [canPostNotifications] уже проверен; Lint не связывает это с [NotificationManagerCompat.notify].
     */
    @SuppressLint("MissingPermission")
    private fun safeNotify(
        nm: NotificationManagerCompat,
        id: Int,
        notification: Notification
    ): Boolean =
        try {
            nm.notify(id, notification)
            true
        } catch (_: SecurityException) {
            false
        }

    private companion object {
        const val PREFS_NAME = "step_milestone_notifications"
        const val KEY_GOAL_DATE = "notified_daily_goal_date"
        const val KEY_BONUS_DATE = "notified_bonus_date"
        const val CHANNEL_ID = "step_milestones"
        const val NOTIF_ID_DAILY_GOAL = 7101
        const val NOTIF_ID_BONUS = 7102
    }
}
