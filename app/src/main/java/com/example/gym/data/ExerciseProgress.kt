package com.example.gym.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_progress",
    indices = [Index(value = ["exerciseId", "date"], unique = true)]
)
data class ExerciseProgress(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseId: Long,
    val date: String, // ISO-8601 yyyy-MM-dd
    val completed: Boolean
)


