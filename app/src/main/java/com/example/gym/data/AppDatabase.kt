package com.example.gym.data

import android.content.Context
import android.content.res.AssetManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Exercise::class, ExerciseProgress::class],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val instance = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    "gym.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Prepopulate asynchronously after creation
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = instance.exerciseDao()
                    val count = dao.getExerciseCount()
                    // Force reload CSV data if database is empty or if we want to refresh data
                    if (count == 0) {
                        val exercises = try {
                            parseCsvExercises(appContext.assets)
                        } catch (_: Throwable) {
                            // Fallback to hardcoded plan if CSV missing
                            defaultMondayExercises()
                        }
                        if (exercises.isNotEmpty()) dao.insertExercises(exercises)
                        android.util.Log.d("DatabaseInit", "Loaded ${exercises.size} exercises from CSV files")
                    } else {
                        android.util.Log.d("DatabaseInit", "Database already has $count exercises, skipping CSV load")
                    }
                }
                instance
            }
        }

        // Method to force reload CSV data (useful for testing/development)
        suspend fun reloadCsvData(context: Context) {
            val dao = getInstance(context).exerciseDao()
            val exercises = try {
                parseCsvExercises(context.assets)
            } catch (e: Exception) {
                android.util.Log.e("DatabaseReload", "Failed to parse CSV files: ${e.message}")
                emptyList()
            }
            if (exercises.isNotEmpty()) {
                // Clear existing data and insert new data
                dao.clearAllExercises()
                dao.insertExercises(exercises)
                android.util.Log.d("DatabaseReload", "Reloaded ${exercises.size} exercises from CSV files")
                
                // Log Saturday and Sunday exercises specifically to verify cleanup
                val saturdayExercises = exercises.filter { it.dayOfWeek == 6 }
                android.util.Log.d("DatabaseReload", "Saturday exercises loaded: ${saturdayExercises.size}")
                saturdayExercises.forEach { exercise ->
                    android.util.Log.d("DatabaseReload", "  Saturday: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                }
                
                val sundayExercises = exercises.filter { it.dayOfWeek == 7 }
                android.util.Log.d("DatabaseReload", "Sunday exercises loaded: ${sundayExercises.size}")
                sundayExercises.forEach { exercise ->
                    android.util.Log.d("DatabaseReload", "  Sunday: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                }
            }
        }

        private fun defaultMondayExercises(): List<Exercise> = listOf(
            Exercise(
                exerciseOrder = "A1",
                exerciseName = "Lat Pulldown Machine",
                primaryMuscleTarget = "Latissimus Dorsi (Back Width)",
                sets = 3,
                reps = "12-15",
                weightRangeKg = "45-65",
                machineSetup = "Wide grip, thigh pads snug, lean back slightly",
                restTimeSeconds = "20 → A2",
                machineAlternative = "Assisted Pull-ups",
                freeWeightAlternative = "Pull-ups + Lat Pullover",
                formCues = "Pull to upper chest, squeeze lats, 2-sec negative",
                backFatFocus = "Builds lat width for V-taper",
                dayOfWeek = 1
            ),
            Exercise(
                exerciseOrder = "A2",
                exerciseName = "Cable Bicep Curls",
                primaryMuscleTarget = "Biceps Brachii (Arm Size)",
                sets = 3,
                reps = "12-15",
                weightRangeKg = "25-40",
                machineSetup = "Low pulley, EZ-curl bar, elbows at sides",
                restTimeSeconds = "90 → A1",
                machineAlternative = "Dumbbell Bicep Curls",
                freeWeightAlternative = "Barbell Curls",
                formCues = "Elbows stable, full ROM, squeeze at top",
                backFatFocus = "Arm size balances back development",
                dayOfWeek = 1
            )
        )
        
        private fun getDefaultExercisesForAllDays(): List<Exercise> {
            val exercises = mutableListOf<Exercise>()
            
            // Tuesday - Cardio & Abs
            exercises.addAll(listOf(
                Exercise(exerciseOrder = "A1", exerciseName = "Treadmill Warm-up", primaryMuscleTarget = "Cardiovascular System", sets = 1, reps = "5-10 min", weightRangeKg = "0", machineSetup = "Moderate pace, gradual increase", restTimeSeconds = "60", machineAlternative = "Elliptical", freeWeightAlternative = "Outdoor Running", formCues = "Maintain steady breathing", backFatFocus = "Cardio endurance", dayOfWeek = 2),
                Exercise(exerciseOrder = "A2", exerciseName = "Ab Crunch Machine", primaryMuscleTarget = "Rectus Abdominis", sets = 3, reps = "15-20", weightRangeKg = "20-40", machineSetup = "Adjust seat height, hands behind head", restTimeSeconds = "30", machineAlternative = "Cable Crunches", freeWeightAlternative = "Floor Crunches", formCues = "Slow controlled movement", backFatFocus = "Core strength", dayOfWeek = 2),
                Exercise(exerciseOrder = "B1", exerciseName = "Elliptical Cardio", primaryMuscleTarget = "Cardiovascular System", sets = 1, reps = "20-30 min", weightRangeKg = "0", machineSetup = "Moderate resistance, steady pace", restTimeSeconds = "60", machineAlternative = "Stationary Bike", freeWeightAlternative = "Cycling", formCues = "Maintain form", backFatFocus = "Cardio endurance", dayOfWeek = 2),
                Exercise(exerciseOrder = "B2", exerciseName = "Captain's Chair", primaryMuscleTarget = "Lower Abs", sets = 3, reps = "12-15", weightRangeKg = "Body Weight", machineSetup = "Support forearms, lift knees", restTimeSeconds = "45", machineAlternative = "Hanging Leg Raises", freeWeightAlternative = "Floor Leg Raises", formCues = "Controlled movement", backFatFocus = "Lower abs", dayOfWeek = 2)
            ))
            
            // Wednesday - Chest & Triceps
            exercises.addAll(listOf(
                Exercise(exerciseOrder = "A1", exerciseName = "Flat DB Press", primaryMuscleTarget = "Pectoralis Major", sets = 3, reps = "8-12", weightRangeKg = "20-35 each", machineSetup = "Bench flat, feet on floor", restTimeSeconds = "90", machineAlternative = "Barbell Bench Press", freeWeightAlternative = "Push-ups", formCues = "Full range of motion", backFatFocus = "Chest development", dayOfWeek = 3),
                Exercise(exerciseOrder = "A2", exerciseName = "Tricep Pushdown", primaryMuscleTarget = "Triceps Brachii", sets = 3, reps = "12-15", weightRangeKg = "25-40", machineSetup = "Cable machine, elbows at sides", restTimeSeconds = "60", machineAlternative = "Overhead DB Extension", freeWeightAlternative = "Dips", formCues = "Control the negative", backFatFocus = "Tricep strength", dayOfWeek = 3),
                Exercise(exerciseOrder = "B1", exerciseName = "Incline DB Press", primaryMuscleTarget = "Upper Chest", sets = 3, reps = "8-12", weightRangeKg = "15-30 each", machineSetup = "Incline bench 30-45 degrees", restTimeSeconds = "90", machineAlternative = "Incline Barbell Press", freeWeightAlternative = "Incline Push-ups", formCues = "Squeeze at top", backFatFocus = "Upper chest", dayOfWeek = 3),
                Exercise(exerciseOrder = "B2", exerciseName = "Cable Overhead Extension", primaryMuscleTarget = "Triceps", sets = 3, reps = "12-15", weightRangeKg = "20-35", machineSetup = "Cable behind head, elbows stable", restTimeSeconds = "60", machineAlternative = "DB Overhead Extension", freeWeightAlternative = "Close-grip Push-ups", formCues = "Full stretch", backFatFocus = "Tricep definition", dayOfWeek = 3)
            ))
            
            // Thursday - Recovery & Stretching
            exercises.addAll(listOf(
                Exercise(exerciseOrder = "A1", exerciseName = "Joint Mobility Warm-up", primaryMuscleTarget = "Full Body", sets = 1, reps = "10-15 min", weightRangeKg = "0", machineSetup = "Gentle movements, all joints", restTimeSeconds = "30", machineAlternative = "Dynamic Stretching", freeWeightAlternative = "Yoga Flow", formCues = "Slow and controlled", backFatFocus = "Mobility", dayOfWeek = 4),
                Exercise(exerciseOrder = "A2", exerciseName = "Neck Side Stretch", primaryMuscleTarget = "Neck Muscles", sets = 3, reps = "30 sec hold", weightRangeKg = "0", machineSetup = "Gentle pull, hold each side", restTimeSeconds = "30", machineAlternative = "Neck Rolls", freeWeightAlternative = "Self-massage", formCues = "No pain", backFatFocus = "Tension relief", dayOfWeek = 4),
                Exercise(exerciseOrder = "B1", exerciseName = "Cat-Cow Stretch", primaryMuscleTarget = "Spine", sets = 3, reps = "10 reps", weightRangeKg = "0", machineSetup = "On hands and knees", restTimeSeconds = "30", machineAlternative = "Spinal Twist", freeWeightAlternative = "Child's Pose", formCues = "Slow movement", backFatFocus = "Spinal mobility", dayOfWeek = 4),
                Exercise(exerciseOrder = "B2", exerciseName = "Hip Flexor Stretch", primaryMuscleTarget = "Hip Flexors", sets = 3, reps = "30 sec hold", weightRangeKg = "0", machineSetup = "Lunge position, push hips forward", restTimeSeconds = "30", machineAlternative = "Pigeon Pose", freeWeightAlternative = "Butterfly Stretch", formCues = "Breathe deeply", backFatFocus = "Hip flexibility", dayOfWeek = 4)
            ))
            
            // Friday - Legs & Shoulders
            exercises.addAll(listOf(
                Exercise(exerciseOrder = "A1", exerciseName = "Squats", primaryMuscleTarget = "Quadriceps", sets = 3, reps = "8-12", weightRangeKg = "Body Weight", machineSetup = "Feet shoulder-width apart", restTimeSeconds = "120", machineAlternative = "Goblet Squats", freeWeightAlternative = "Wall Sits", formCues = "Full depth", backFatFocus = "Leg strength", dayOfWeek = 5),
                Exercise(exerciseOrder = "A2", exerciseName = "Overhead Press DB", primaryMuscleTarget = "Deltoids", sets = 3, reps = "8-12", weightRangeKg = "12-20 each", machineSetup = "Start at shoulders, press up", restTimeSeconds = "90", machineAlternative = "Barbell Press", freeWeightAlternative = "Pike Push-ups", formCues = "Core engaged", backFatFocus = "Shoulder strength", dayOfWeek = 5),
                Exercise(exerciseOrder = "B1", exerciseName = "Forward Lunges", primaryMuscleTarget = "Quadriceps", sets = 3, reps = "10 each", weightRangeKg = "Body Weight", machineSetup = "Step forward, lower back knee", restTimeSeconds = "90", machineAlternative = "Reverse Lunges", freeWeightAlternative = "Bulgarian Split Squats", formCues = "Controlled movement", backFatFocus = "Leg balance", dayOfWeek = 5),
                Exercise(exerciseOrder = "B2", exerciseName = "Lateral Raise DB", primaryMuscleTarget = "Deltoids", sets = 3, reps = "12-15", weightRangeKg = "5-12 each", machineSetup = "Arms to sides, lift to shoulder height", restTimeSeconds = "60", machineAlternative = "Cable Lateral Raises", freeWeightAlternative = "Band Lateral Raises", formCues = "No momentum", backFatFocus = "Shoulder width", dayOfWeek = 5)
            ))
            
            // Saturday - CrossFit & Cardio
            exercises.addAll(listOf(
                Exercise(exerciseOrder = "A1", exerciseName = "Burpees", primaryMuscleTarget = "Full Body", sets = 3, reps = "8-12", weightRangeKg = "Body Weight", machineSetup = "Push-up to jump sequence", restTimeSeconds = "60", machineAlternative = "Modified Burpees", freeWeightAlternative = "Mountain Climbers", formCues = "Full range", backFatFocus = "Cardio strength", dayOfWeek = 6),
                Exercise(exerciseOrder = "A2", exerciseName = "Kettlebell Swings", primaryMuscleTarget = "Posterior Chain", sets = 3, reps = "15-20", weightRangeKg = "15-25", machineSetup = "Hip hinge movement", restTimeSeconds = "90", machineAlternative = "Deadlifts", freeWeightAlternative = "Romanian Deadlifts", formCues = "Hip drive", backFatFocus = "Power development", dayOfWeek = 6),
                Exercise(exerciseOrder = "B1", exerciseName = "Box Jumps", primaryMuscleTarget = "Lower Body Power", sets = 3, reps = "8-10", weightRangeKg = "Body Weight", machineSetup = "Land softly, step down", restTimeSeconds = "90", machineAlternative = "Step-ups", freeWeightAlternative = "Jump Squats", formCues = "Controlled landing", backFatFocus = "Explosive power", dayOfWeek = 6),
                Exercise(exerciseOrder = "B2", exerciseName = "Battle Ropes", primaryMuscleTarget = "Cardiovascular", sets = 3, reps = "30 sec", weightRangeKg = "0", machineSetup = "Alternating waves", restTimeSeconds = "60", machineAlternative = "Jump Rope", freeWeightAlternative = "High Knees", formCues = "Full intensity", backFatFocus = "Cardio endurance", dayOfWeek = 6)
            ))
            
            return exercises
        }

        private fun defaultWeeklyPlan(): Map<Int, List<String>> = mapOf(
            // Monday — Back + Biceps + Forearms
            1 to listOf(
                "Lat Pulldown",
                "Bent Over Row/Seated Row",
                "Straight Arm Pulldown",
                "Dumbbell Shrugs",
                "Back Extension",
                "Supination Curl (DB)",
                "Hammer Curl",
                "Concentration Curl",
                "Barbell Bicep Curl",
                "Gorilla Gripper/Forearm Work"
            ),
            // Tuesday — Cardio + Abs (Machine Focus)
            2 to listOf(
                "Treadmill Warm-up",
                "Elliptical Cardio",
                "Stationary Bike",
                "Ab Crunch Machine",
                "Captain's Chair",
                "Rowing Machine",
                "Rotary Torso Machine",
                "Decline Bench Crunches"
            ),
            // Wednesday — Chest + Triceps
            3 to listOf(
                "Flat DB Press",
                "Incline DB Press",
                "Decline DB Press",
                "Pec Dec Fly",
                "Cable Crossover",
                "Push-ups",
                "Bar Dips",
                "Tricep Pushdown",
                "Cable Overhead Extension",
                "DB Tricep Extension",
                "Tricep Press Machine"
            ),
            // Thursday — Recovery + Stretching
            4 to listOf(
                "Joint Mobility Warm-up",
                "Dynamic Stretching",
                "Light Cardio",
                "Neck Side Stretch",
                "Shoulder Cross Stretch",
                "Doorway Chest Stretch",
                "Cat-Cow Stretch",
                "Seated Spinal Twist",
                "Hip Flexor Stretch",
                "Standing Quad Stretch",
                "Seated Forward Fold + Cool Down"
            ),
            // Friday — Legs + Shoulders (Heavy Day)
            5 to listOf(
                "Squats",
                "Forward Lunges",
                "Reverse Lunges",
                "Leg Press",
                "Leg Extension",
                "Hip Thrust",
                "Standing Calf Raise",
                "Seated Calf Raise",
                "Leg Curl",
                "Overhead Press DB",
                "Overhead Press BB",
                "Lateral Raise DB",
                "Reverse Fly",
                "External Rotation DB"
            ),
            // Saturday — CrossFit + Cardio (High Intensity)
            6 to listOf(
                "Cardio Warm-up",
                "Movement Prep",
                "Burpees",
                "Kettlebell Swings",
                "Box Jumps",
                "Push-ups",
                "Mountain Climbers",
                "Squat Jumps",
                "Battle Ropes",
                "Plank Hold",
                "Dumbbell Thrusters",
                "Final Cardio Session"
            )
        )

        private fun parseCsvExercises(assets: AssetManager): List<Exercise> {
            val exercises = mutableListOf<Exercise>()
            android.util.Log.d("CSVParser", "Starting to parse CSV exercises...")
            
            // Parse Monday CSV
            try {
                val mondayStream = assets.open("MONDAY.csv")
                val mondayLines = mondayStream.bufferedReader().use { it.readLines() }
                if (mondayLines.isNotEmpty()) {
                    val header = mondayLines.first()
                    val idx = headerColumns(header)
                    val mondayExercises = mondayLines.drop(1)
                        .asSequence()
                        .filter { it.isNotBlank() }
                        .map { parseCsvLine(it) }
                        .mapNotNull { cols ->
                            fun col(name: String): String? = idx[name]?.let { i -> cols.getOrNull(i) }?.trim()?.ifEmpty { null }
                            
                            val exerciseOrder = col("Exercise_Order") ?: return@mapNotNull null
                            val exerciseName = col("Exercise_Name") ?: return@mapNotNull null
                            val primaryMuscleTarget = col("Primary_Muscle_Target") ?: return@mapNotNull null
                            val setsStr = col("Sets") ?: return@mapNotNull null
                            val sets = setsStr.toIntOrNull() ?: return@mapNotNull null
                            val reps = col("Reps") ?: return@mapNotNull null
                            val weightRangeKg = col("Weight_Range_kg") ?: return@mapNotNull null
                            
                            val exercise = Exercise(
                                exerciseOrder = exerciseOrder,
                                exerciseName = exerciseName,
                                primaryMuscleTarget = primaryMuscleTarget,
                                sets = sets,
                                reps = reps,
                                weightRangeKg = weightRangeKg,
                                machineSetup = col("Machine_Setup"),
                                restTimeSeconds = col("Rest_Time_Seconds"),
                                machineAlternative = col("Machine_Alternative"),
                                freeWeightAlternative = col("Free_Weight_Alternative"),
                                formCues = col("Form_Cues"),
                                backFatFocus = col("Back_Fat_Focus"),
                                dayOfWeek = 1 // Monday
                            )
                            android.util.Log.d("CSVParser", "Parsed exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                            exercise
                        }
                        .toList()
                    exercises.addAll(mondayExercises)
                }
            } catch (e: Exception) {
                // Fallback to default exercises if CSV parsing fails
                val defaultExercises = defaultMondayExercises()
                exercises.addAll(defaultExercises)
            }
            
            // Parse Tuesday CSV
            try {
                val tuesdayStream = assets.open("TUESDAY.csv")
                val tuesdayLines = tuesdayStream.bufferedReader().use { it.readLines() }
                if (tuesdayLines.isNotEmpty()) {
                    val header = tuesdayLines.first()
                    val idx = headerColumns(header)
                    val tuesdayExercises = tuesdayLines.drop(1)
                        .asSequence()
                        .filter { it.isNotBlank() }
                        .map { parseCsvLine(it) }
                        .mapNotNull { cols ->
                            fun col(name: String): String? = idx[name]?.let { i -> cols.getOrNull(i) }?.trim()?.ifEmpty { null }
                            
                            val exerciseOrder = col("Exercise_Order") ?: return@mapNotNull null
                            val exerciseName = col("Exercise_Name") ?: return@mapNotNull null
                            val primaryMuscleTarget = col("Primary_Muscle_Target") ?: return@mapNotNull null
                            val setsStr = col("Sets") ?: return@mapNotNull null
                            val sets = setsStr.toIntOrNull() ?: return@mapNotNull null
                            val reps = col("Reps") ?: return@mapNotNull null
                            val weightRangeKg = col("Weight_Range_kg") ?: return@mapNotNull null
                            
                            val exercise = Exercise(
                                exerciseOrder = exerciseOrder,
                                exerciseName = exerciseName,
                                primaryMuscleTarget = primaryMuscleTarget,
                                sets = sets,
                                reps = reps,
                                weightRangeKg = weightRangeKg,
                                machineSetup = col("Machine_Setup"),
                                restTimeSeconds = col("Rest_Time_Seconds"),
                                machineAlternative = col("Machine_Alternative"),
                                freeWeightAlternative = col("Free_Weight_Alternative"),
                                formCues = col("Form_Cues"),
                                chestDevelopmentFocus = col("Chest_Development_Focus"),
                                dayOfWeek = 2 // Tuesday
                            )
                            android.util.Log.d("CSVParser", "Parsed exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                            exercise
                        }
                        .toList()
                    exercises.addAll(tuesdayExercises)
                }
            } catch (e: Exception) {
                android.util.Log.w("CSVParser", "Failed to parse TUESDAY.csv: ${e.message}")
            }
            
            // Parse Wednesday CSV
            try {
                val wednesdayStream = assets.open("WEDNESDAY.csv")
                val wednesdayLines = wednesdayStream.bufferedReader().use { it.readLines() }
                if (wednesdayLines.isNotEmpty()) {
                    val header = wednesdayLines.first()
                    val idx = headerColumns(header)
                    val wednesdayExercises = wednesdayLines.drop(1)
                        .asSequence()
                        .filter { it.isNotBlank() }
                        .map { parseCsvLine(it) }
                        .mapNotNull { cols ->
                            fun col(name: String): String? = idx[name]?.let { i -> cols.getOrNull(i) }?.trim()?.ifEmpty { null }
                            
                            val exerciseOrder = col("Exercise_Order") ?: return@mapNotNull null
                            val exerciseName = col("Exercise_Name") ?: return@mapNotNull null
                            val primaryMuscleTarget = col("Primary_Muscle_Target") ?: return@mapNotNull null
                            val setsStr = col("Sets") ?: return@mapNotNull null
                            val sets = setsStr.toIntOrNull() ?: return@mapNotNull null
                            val reps = col("Reps") ?: return@mapNotNull null
                            val weightRangeKg = col("Weight_Range_kg") ?: return@mapNotNull null
                            
                            val exercise = Exercise(
                                exerciseOrder = exerciseOrder,
                                exerciseName = exerciseName,
                                primaryMuscleTarget = primaryMuscleTarget,
                                sets = sets,
                                reps = reps,
                                weightRangeKg = weightRangeKg,
                                machineSetup = col("Machine_Setup"),
                                restTimeSeconds = col("Rest_Time_Seconds"),
                                machineAlternative = col("Machine_Alternative"),
                                freeWeightAlternative = col("Free_Weight_Alternative"),
                                formCues = col("Form_Cues"),
                                calorieBurnFocus = col("Calorie_Burn_Focus"),
                                dayOfWeek = 3 // Wednesday
                            )
                            android.util.Log.d("CSVParser", "Parsed exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                            exercise
                        }
                        .toList()
                    exercises.addAll(wednesdayExercises)
                }
            } catch (e: Exception) {
                android.util.Log.w("CSVParser", "Failed to parse WEDNESDAY.csv: ${e.message}")
            }
            
            // Parse Thursday CSV
            try {
                val thursdayStream = assets.open("THURSDAY.csv")
                val thursdayLines = thursdayStream.bufferedReader().use { it.readLines() }
                if (thursdayLines.isNotEmpty()) {
                    val header = thursdayLines.first()
                    val idx = headerColumns(header)
                    val thursdayExercises = thursdayLines.drop(1)
                        .asSequence()
                        .filter { it.isNotBlank() }
                        .map { parseCsvLine(it) }
                        .mapNotNull { cols ->
                            fun col(name: String): String? = idx[name]?.let { i -> cols.getOrNull(i) }?.trim()?.ifEmpty { null }
                            
                            val exerciseOrder = col("Exercise_Order") ?: return@mapNotNull null
                            val exerciseName = col("Exercise_Name") ?: return@mapNotNull null
                            val primaryMuscleTarget = col("Primary_Muscle_Target") ?: return@mapNotNull null
                            val setsStr = col("Sets") ?: return@mapNotNull null
                            val sets = setsStr.toIntOrNull() ?: return@mapNotNull null
                            val reps = col("Reps") ?: return@mapNotNull null
                            val weightRangeKg = col("Weight_Range_kg") ?: return@mapNotNull null
                            
                            val exercise = Exercise(
                                exerciseOrder = exerciseOrder,
                                exerciseName = exerciseName,
                                primaryMuscleTarget = primaryMuscleTarget,
                                sets = sets,
                                reps = reps,
                                weightRangeKg = weightRangeKg,
                                machineSetup = col("Machine_Setup"),
                                restTimeSeconds = col("Rest_Time_Seconds"),
                                machineAlternative = col("Machine_Alternative"),
                                freeWeightAlternative = col("Free_Weight_Alternative"),
                                formCues = col("Form_Cues"),
                                vTaperCoreFocus = col("VTaper_Core_Focus"),
                                dayOfWeek = 4 // Thursday
                            )
                            android.util.Log.d("CSVParser", "Parsed exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                            exercise
                        }
                        .toList()
                    exercises.addAll(thursdayExercises)
                }
            } catch (e: Exception) {
                android.util.Log.w("CSVParser", "Failed to parse THURSDAY.csv: ${e.message}")
            }
            
            // Parse Friday CSV
            try {
                val fridayStream = assets.open("FRIDAY.csv")
                val fridayLines = fridayStream.bufferedReader().use { it.readLines() }
                if (fridayLines.isNotEmpty()) {
                    val header = fridayLines.first()
                    val idx = headerColumns(header)
                    val fridayExercises = fridayLines.drop(1)
                        .asSequence()
                        .filter { it.isNotBlank() }
                        .map { parseCsvLine(it) }
                        .mapNotNull { cols ->
                            fun col(name: String): String? = idx[name]?.let { i -> cols.getOrNull(i) }?.trim()?.ifEmpty { null }
                            
                            val exerciseOrder = col("Exercise_Order") ?: return@mapNotNull null
                            val exerciseName = col("Exercise_Name") ?: return@mapNotNull null
                            val primaryMuscleTarget = col("Primary_Muscle_Target") ?: return@mapNotNull null
                            val setsStr = col("Sets") ?: return@mapNotNull null
                            val sets = setsStr.toIntOrNull() ?: return@mapNotNull null
                            val reps = col("Reps") ?: return@mapNotNull null
                            val weightRangeKg = col("Weight_Range_kg") ?: return@mapNotNull null
                            
                            val exercise = Exercise(
                                exerciseOrder = exerciseOrder,
                                exerciseName = exerciseName,
                                primaryMuscleTarget = primaryMuscleTarget,
                                sets = sets,
                                reps = reps,
                                weightRangeKg = weightRangeKg,
                                machineSetup = col("Machine_Setup"),
                                restTimeSeconds = col("Rest_Time_Seconds"),
                                machineAlternative = col("Machine_Alternative"),
                                freeWeightAlternative = col("Free_Weight_Alternative"),
                                formCues = col("Form_Cues"),
                                definitionFocus = col("Definition_Focus"),
                                dayOfWeek = 5 // Friday
                            )
                            android.util.Log.d("CSVParser", "Parsed exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                            exercise
                        }
                        .toList()
                    exercises.addAll(fridayExercises)
                }
            } catch (e: Exception) {
                android.util.Log.w("CSVParser", "Failed to parse FRIDAY.csv: ${e.message}")
            }
            
            // Parse Saturday CSV (different structure - circuit training)
            try {
                val saturdayStream = assets.open("SATURDAY.csv")
                val saturdayLines = saturdayStream.bufferedReader().use { it.readLines() }
                android.util.Log.d("CSVParser", "Saturday CSV lines: ${saturdayLines.size}")
                if (saturdayLines.isNotEmpty()) {
                    val header = saturdayLines.first()
                    val idx = headerColumns(header)
                    android.util.Log.d("CSVParser", "Saturday header: $header")
                    android.util.Log.d("CSVParser", "Saturday columns: $idx")
                    val saturdayExercises = saturdayLines.drop(1)
                        .asSequence()
                        .filter { it.isNotBlank() }
                        .map { parseCsvLine(it) }
                        .mapNotNull { cols ->
                            fun col(name: String): String? = idx[name]?.let { i -> cols.getOrNull(i) }?.trim()?.ifEmpty { null }
                            
                            val exerciseOrder = col("Station_Number") ?: return@mapNotNull null
                            val exerciseName = col("Exercise_Name") ?: return@mapNotNull null
                            val primaryMuscleTarget = col("Primary_Muscle_Target") ?: return@mapNotNull null
                            val roundsTotal = col("Rounds_Total") ?: return@mapNotNull null
                            // Extract number from "3 rounds" format
                            val sets = roundsTotal.replace("rounds", "").trim().toIntOrNull() ?: 3
                            val reps = col("Target_Reps_Per_45sec") ?: return@mapNotNull null
                            val weightRangeKg = col("Weight_Range_kg") ?: return@mapNotNull null
                            
                            val exercise = Exercise(
                                exerciseOrder = exerciseOrder,
                                exerciseName = exerciseName,
                                primaryMuscleTarget = primaryMuscleTarget,
                                sets = sets,
                                reps = reps,
                                weightRangeKg = weightRangeKg,
                                machineSetup = col("Machine_Setup"),
                                restTimeSeconds = col("Rest_Duration_Seconds"),
                                machineAlternative = col("Machine_Alternative"),
                                freeWeightAlternative = col("Free_Weight_Alternative"),
                                formCues = col("Form_Focus"),
                                fatBurnStrategy = col("Fat_Burn_Strategy"),
                                workDurationSeconds = col("Work_Duration_Seconds"),
                                roundsTotal = roundsTotal,
                                dayOfWeek = 6 // Saturday
                            )
                            android.util.Log.d("CSVParser", "Parsed exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                            exercise
                        }
                        .toList()
                    exercises.addAll(saturdayExercises)
                }
            } catch (e: Exception) {
                android.util.Log.w("CSVParser", "Failed to parse SATURDAY.csv: ${e.message}")
            }
            
            // Parse Sunday CSV (different structure - home workout with bands)
            try {
                val sundayStream = assets.open("SUNDAY.csv")
                val sundayLines = sundayStream.bufferedReader().use { it.readLines() }
                android.util.Log.d("CSVParser", "Sunday CSV lines: ${sundayLines.size}")
                if (sundayLines.isNotEmpty()) {
                    val header = sundayLines.first()
                    val idx = headerColumns(header)
                    val sundayExercises = sundayLines.drop(1)
                        .asSequence()
                        .filter { it.isNotBlank() }
                        .map { parseCsvLine(it) }
                        .mapNotNull { cols ->
                            fun col(name: String): String? = idx[name]?.let { i -> cols.getOrNull(i) }?.trim()?.ifEmpty { null }
                            
                            val exerciseOrder = col("Exercise_Order") ?: return@mapNotNull null
                            val exerciseName = col("Exercise_Name") ?: return@mapNotNull null
                            val primaryMuscleTarget = col("Primary_Muscle_Target") ?: return@mapNotNull null
                            val setsStr = col("Sets") ?: return@mapNotNull null
                            val sets = setsStr.toIntOrNull() ?: return@mapNotNull null
                            val reps = col("Reps") ?: return@mapNotNull null
                            val weightRangeKg = col("Band_Resistance") ?: return@mapNotNull null
                            
                            val exercise = Exercise(
                                exerciseOrder = exerciseOrder,
                                exerciseName = exerciseName,
                                primaryMuscleTarget = primaryMuscleTarget,
                                sets = sets,
                                reps = reps,
                                weightRangeKg = weightRangeKg,
                                machineSetup = col("Setup_Instructions"),
                                restTimeSeconds = col("Rest_Time_Seconds"),
                                machineAlternative = col("Bodyweight_Alternative"),
                                freeWeightAlternative = col("Free_Weight_Alternative"),
                                formCues = col("Form_Cues"),
                                homeWorkoutBenefits = col("Home_Workout_Benefits"),
                                progressionTips = col("Progression_Tips"),
                                dayOfWeek = 7 // Sunday
                            )
                            android.util.Log.d("CSVParser", "Parsed exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
                            exercise
                        }
                        .toList()
                    exercises.addAll(sundayExercises)
                }
            } catch (e: Exception) {
                android.util.Log.w("CSVParser", "Failed to parse SUNDAY.csv: ${e.message}")
            }
            
            // Note: We now load all days from CSV files, so no need for default exercises
            android.util.Log.d("CSVParser", "Total exercises parsed: ${exercises.size}")
            return exercises
        }

        private fun headerColumns(header: String): Map<String, Int> =
            parseCsvLine(header).mapIndexed { i, h -> h to i }.toMap()

        private fun mapDayToInt(day: String): Int? = when (day.trim().lowercase()) {
            "monday" -> 1
            "tuesday" -> 2
            "wednesday" -> 3
            "thursday" -> 4
            "friday" -> 5
            "saturday" -> 6
            "sunday" -> 7
            else -> null
        }

        // Minimal CSV parser that handles quoted fields and commas within quotes
        private fun parseCsvLine(line: String): List<String> {
            val result = ArrayList<String>()
            val sb = StringBuilder()
            var inQuotes = false
            var i = 0
            while (i < line.length) {
                val c = line[i]
                when (c) {
                    '"' -> {
                        if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                            sb.append('"')
                            i++
                        } else {
                            inQuotes = !inQuotes
                        }
                    }
                    ',' -> {
                        if (inQuotes) sb.append(c) else {
                            result.add(sb.toString())
                            sb.setLength(0)
                        }
                    }
                    else -> sb.append(c)
                }
                i++
            }
            result.add(sb.toString())
            return result
        }
    }
}


