package com.example.diplom.domain

/**
 * Шаги ≥ 1,5× дневной цели (при [dailyGoal] > 0): единый порог для
 * — серии тренировок на вкладке наград,
 * — серии и бонуса XP в геймификации,
 * — предложения «конвертировать шаги в тренировку».
 */
fun stepsMeetWorkoutStreakAlternative(steps: Int, dailyGoal: Int): Boolean =
    dailyGoal > 0 && steps * 2 >= dailyGoal * 3
