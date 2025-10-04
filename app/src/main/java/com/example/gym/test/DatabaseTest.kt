package com.example.gym.test

import android.content.Context
import com.example.gym.data.AppDatabase
import kotlinx.coroutines.runBlocking

object DatabaseTest {
    fun testExerciseData(context: Context) {
        runBlocking {
            val database = AppDatabase.getInstance(context)
            val exercises = database.exerciseDao().getExercisesForDay(1)
            
            exercises.collect { exerciseList ->
                android.util.Log.d("DatabaseTest", "Found ${exerciseList.size} exercises")
                exerciseList.forEach { exercise ->
                    android.util.Log.d("DatabaseTest", "Exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                }
            }
        }
    }
}
