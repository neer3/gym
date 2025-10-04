package com.example.gym.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.example.gym.data.AppDatabase
import com.example.gym.data.Exercise
import com.example.gym.utils.DayUtils

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    
    private val _exercises = MutableLiveData<List<Exercise>>()
    val exercises: LiveData<List<Exercise>> = _exercises
    
    private val _selectedExercise = MutableLiveData<Exercise?>()
    val selectedExercise: LiveData<Exercise?> = _selectedExercise
    
    private val _currentDayName = MutableLiveData<String>()
    val currentDayName: LiveData<String> = _currentDayName
    
    private val _workoutFocus = MutableLiveData<String>()
    val workoutFocus: LiveData<String> = _workoutFocus
    
    private val _isRestDay = MutableLiveData<Boolean>()
    val isRestDay: LiveData<Boolean> = _isRestDay

    init {
        loadCurrentDayExercises()
    }
    
    private fun loadCurrentDayExercises() {
        val currentDay = DayUtils.getCurrentDayOfWeek()
        val dayName = DayUtils.getCurrentDayName()
        val focus = DayUtils.getCurrentWorkoutFocus()
        val restDay = DayUtils.isRestDay(currentDay)
        
        _currentDayName.value = dayName
        _workoutFocus.value = focus
        _isRestDay.value = restDay
        
        // Load exercises for current day
        database.exerciseDao().getExercisesForDay(currentDay).asLiveData().observeForever { exerciseList ->
            _exercises.value = exerciseList
        }
    }
    
    fun selectExercise(exercise: Exercise) {
        _selectedExercise.value = exercise
    }
    
    fun refreshCurrentDay() {
        loadCurrentDayExercises()
    }
}