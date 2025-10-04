package com.example.gym.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExerciseRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val dao = database.exerciseDao()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getExercisesForDay(dayOfWeek: Int): Flow<List<Exercise>> = dao.getExercisesForDay(dayOfWeek)

    fun getProgressForDate(date: LocalDate): Flow<List<ExerciseProgress>> =
        dao.getProgressForDate(date.format(dateFormatter))

    suspend fun toggleProgress(exerciseId: Long, date: LocalDate, completed: Boolean) {
        val iso = date.format(dateFormatter)
        val current = dao.getProgressForExerciseOnDate(exerciseId, iso)
        val updated = ExerciseProgress(
            id = current?.id ?: 0,
            exerciseId = exerciseId,
            date = iso,
            completed = completed
        )
        dao.upsertProgress(updated)
    }

    fun getDailySummary(start: LocalDate, end: LocalDate): Flow<List<DailySummaryRow>> =
        dao.getDailySummary(start.format(dateFormatter), end.format(dateFormatter))
}


