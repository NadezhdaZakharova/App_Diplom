package com.example.diplom.domain

/**
 * Показ системных уведомлений при достижении дневной цели по шагам
 * и при пороге 1,5× (бонус). Не более одного уведомления каждого типа за календарный день.
 */
fun interface StepMilestoneNotifier {
    fun onStepTotalsUpdated(todaySteps: Int, dailyGoal: Int, todayIso: String)
}
