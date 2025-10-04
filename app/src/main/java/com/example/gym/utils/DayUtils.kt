package com.example.gym.utils

import java.util.Calendar

object DayUtils {
    
    /**
     * Get the current day of week as an integer (1=Monday, 2=Tuesday, ..., 6=Saturday, 7=Sunday)
     */
    fun getCurrentDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Convert from Calendar format (1=Sunday, 2=Monday, ...) to our format (1=Monday, 2=Tuesday, ...)
        return when (dayOfWeek) {
            Calendar.SUNDAY -> 7
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1 // Default to Monday
        }
    }
    
    /**
     * Get the day name for a given day of week integer
     */
    fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Monday"
            2 -> "Tuesday"
            3 -> "Wednesday"
            4 -> "Thursday"
            5 -> "Friday"
            6 -> "Saturday"
            7 -> "Sunday"
            else -> "Monday"
        }
    }
    
    /**
     * Get the workout focus for a given day
     */
    fun getWorkoutFocus(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Back & Biceps" // Monday
            2 -> "Cardio & Abs" // Tuesday
            3 -> "Chest & Triceps" // Wednesday
            4 -> "Recovery & Stretching" // Thursday
            5 -> "Legs & Shoulders" // Friday
            6 -> "CrossFit & Cardio" // Saturday
            7 -> "Rest Day" // Sunday
            else -> "Back & Biceps"
        }
    }
    
    /**
     * Get the current day name
     */
    fun getCurrentDayName(): String {
        return getDayName(getCurrentDayOfWeek())
    }
    
    /**
     * Get the current workout focus
     */
    fun getCurrentWorkoutFocus(): String {
        return getWorkoutFocus(getCurrentDayOfWeek())
    }
    
    /**
     * Check if it's a rest day (Sunday)
     */
    fun isRestDay(dayOfWeek: Int = getCurrentDayOfWeek()): Boolean {
        return dayOfWeek == 7
    }
}
