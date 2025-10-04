package com.example.gym.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.gym.data.AppDatabase
import com.example.gym.databinding.FragmentExerciseDetailBinding
import kotlinx.coroutines.launch

class ExerciseDetailFragment : Fragment() {
    
    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var database: AppDatabase
    private var exerciseId: Long = -1
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        database = AppDatabase.getInstance(requireContext())
        
        // Get exercise ID from arguments
        exerciseId = arguments?.getLong("exerciseId", -1) ?: -1
        
        if (exerciseId != -1L) {
            loadExerciseDetails()
        }
    }
    
    private fun loadExerciseDetails() {
        lifecycleScope.launch {
            val exercise = database.exerciseDao().getExerciseById(exerciseId)
            exercise?.let { ex ->
                populateExerciseDetails(ex)
            }
        }
    }
    
    private fun populateExerciseDetails(exercise: com.example.gym.data.Exercise) {
        binding.exerciseOrder.text = exercise.exerciseOrder
        binding.exerciseName.text = exercise.exerciseName
        binding.primaryMuscleTarget.text = exercise.primaryMuscleTarget
        binding.sets.text = exercise.sets.toString()
        binding.reps.text = exercise.reps
        binding.weightRange.text = "${exercise.weightRangeKg} kg"
        binding.restTime.text = exercise.restTimeSeconds ?: "Not specified"
        binding.machineSetup.text = exercise.machineSetup ?: "Not specified"
        binding.formCues.text = exercise.formCues ?: "Not specified"
        binding.machineAlternative.text = exercise.machineAlternative ?: "Not specified"
        binding.freeWeightAlternative.text = exercise.freeWeightAlternative ?: "Not specified"
        
        // Set focus content based on day of week
        when (exercise.dayOfWeek) {
            1 -> { // Monday
                binding.focusTitle.text = "Back & Fat Focus"
                binding.focusContent.text = exercise.backFatFocus ?: "Not specified"
            }
            2 -> { // Tuesday
                binding.focusTitle.text = "Chest Development Focus"
                binding.focusContent.text = exercise.chestDevelopmentFocus ?: "Not specified"
            }
            3 -> { // Wednesday
                binding.focusTitle.text = "Calorie Burn Focus"
                binding.focusContent.text = exercise.calorieBurnFocus ?: "Not specified"
            }
            4 -> { // Thursday
                binding.focusTitle.text = "V-Taper & Core Focus"
                binding.focusContent.text = exercise.vTaperCoreFocus ?: "Not specified"
            }
            5 -> { // Friday
                binding.focusTitle.text = "Definition Focus"
                binding.focusContent.text = exercise.definitionFocus ?: "Not specified"
            }
            6 -> { // Saturday - Circuit Training
                binding.focusTitle.text = "Fat Burn Strategy"
                val fatBurnStrategy = exercise.fatBurnStrategy ?: "Not specified"
                val workDuration = exercise.workDurationSeconds ?: "Not specified"
                val roundsTotal = exercise.roundsTotal ?: "Not specified"
                binding.focusContent.text = "$fatBurnStrategy\n\nWork Duration: ${workDuration}s\nRounds: $roundsTotal"
            }
            7 -> { // Sunday - Home Workout
                binding.focusTitle.text = "Home Workout Benefits"
                val homeWorkoutBenefits = exercise.homeWorkoutBenefits ?: "Not specified"
                val progressionTips = exercise.progressionTips ?: "Not specified"
                binding.focusContent.text = "$homeWorkoutBenefits\n\nProgression Tips: $progressionTips"
            }
            else -> {
                binding.focusTitle.text = "Training Focus"
                binding.focusContent.text = "Focus information not available"
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance(exerciseId: Long): ExerciseDetailFragment {
            val fragment = ExerciseDetailFragment()
            val args = Bundle()
            args.putLong("exerciseId", exerciseId)
            fragment.arguments = args
            return fragment
        }
    }
}
