package com.example.gym.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gym.data.Exercise
import com.example.gym.data.ExerciseProgress
import com.example.gym.data.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class DayUiState(
    val dayOfWeek: Int,
    val date: LocalDate,
    val exercises: List<Exercise> = emptyList(),
    val progressByExerciseId: Map<Long, ExerciseProgress> = emptyMap()
)

class ProgressViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ExerciseRepository(app)

    private val _selectedDay = MutableStateFlow(1) // Monday default
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _uiState = MutableStateFlow(DayUiState(dayOfWeek = 1, date = LocalDate.now()))
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(selectedDay, selectedDate) { day, date -> day to date }
                .flatMapLatest { (day, date) ->
                    combine(
                        repository.getExercisesForDay(day),
                        repository.getProgressForDate(date)
                    ) { exercises, progress -> exercises to progress }
                }
                .collect { (exercises, progress) ->
                    _uiState.update { old ->
                        old.copy(
                            exercises = exercises,
                            progressByExerciseId = progress.associateBy { it.exerciseId }
                        )
                    }
                }
        }
    }

    fun selectDay(dayOfWeek: Int) {
        _selectedDay.value = dayOfWeek
        val mondayOfWeek = LocalDate.now().with(DayOfWeek.MONDAY)
        val selectedDate = mondayOfWeek.plusDays((dayOfWeek - 1).toLong())
        _selectedDate.value = selectedDate
        _uiState.update { it.copy(dayOfWeek = dayOfWeek, date = selectedDate) }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        _uiState.update { it.copy(date = date) }
    }

    fun toggle(exerciseId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleProgress(exerciseId, _selectedDate.value, completed)
        }
    }
}


