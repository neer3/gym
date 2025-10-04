package com.example.gym.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseOrder: String, // A1, A2, B1, B2, etc. or Station Number
    val exerciseName: String,
    val primaryMuscleTarget: String,
    val sets: Int,
    val reps: String, // e.g., "12-15" or "Target_Reps_Per_45sec"
    val weightRangeKg: String, // e.g., "45-65" or "Band_Resistance"
    val machineSetup: String? = null, // or "Setup_Instructions"
    val restTimeSeconds: String? = null, // e.g., "20 → A2" or "Rest_Duration_Seconds"
    val machineAlternative: String? = null, // or "Bodyweight_Alternative"
    val freeWeightAlternative: String? = null,
    val formCues: String? = null,
    val backFatFocus: String? = null, // For Monday
    val chestDevelopmentFocus: String? = null, // For Tuesday
    val calorieBurnFocus: String? = null, // For Wednesday
    val vTaperCoreFocus: String? = null, // For Thursday
    val definitionFocus: String? = null, // For Friday
    val fatBurnStrategy: String? = null, // For Saturday
    val workDurationSeconds: String? = null, // For Saturday
    val roundsTotal: String? = null, // For Saturday
    val homeWorkoutBenefits: String? = null, // For Sunday
    val progressionTips: String? = null, // For Sunday
    val dayOfWeek: Int = 1 // Default to Monday for now
)


