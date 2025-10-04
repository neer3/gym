package com.example.gym.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Long): Exercise?

    @Query("SELECT * FROM exercises WHERE dayOfWeek = :dayOfWeek ORDER BY id ASC")
    fun getExercisesForDay(dayOfWeek: Int): Flow<List<Exercise>>

    @Query("SELECT * FROM exercise_progress WHERE date = :date")
    fun getProgressForDate(date: String): Flow<List<ExerciseProgress>>

    @Query("SELECT * FROM exercise_progress WHERE exerciseId = :exerciseId AND date = :date LIMIT 1")
    suspend fun getProgressForExerciseOnDate(exerciseId: Long, date: String): ExerciseProgress?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("DELETE FROM exercises")
    suspend fun clearAllExercises()

    @Query("DELETE FROM exercises WHERE dayOfWeek = :dayOfWeek")
    suspend fun clearExercisesForDay(dayOfWeek: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ExerciseProgress)

    @Update
    suspend fun updateProgress(progress: ExerciseProgress)

    // History queries
    @Query("SELECT date, COUNT(CASE WHEN completed THEN 1 END) as completedCount, COUNT(*) as totalCount FROM exercise_progress WHERE date BETWEEN :startDate AND :endDate GROUP BY date ORDER BY date DESC")
    fun getDailySummary(startDate: String, endDate: String): Flow<List<DailySummaryRow>>
}

data class DailySummaryRow(
    val date: String,
    val completedCount: Int,
    val totalCount: Int
)


