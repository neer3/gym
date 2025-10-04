package com.example.gym.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.gym.data.DailySummaryRow
import com.example.gym.data.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ExerciseRepository(app)

    private val _rangeDays = MutableStateFlow(7)
    val rangeDays: StateFlow<Int> = _rangeDays.asStateFlow()

    fun setRange(days: Int) { _rangeDays.value = days }

    fun summaries(): kotlinx.coroutines.flow.Flow<List<DailySummaryRow>> =
        _rangeDays.flatMapLatest { days ->
            val end = LocalDate.now()
            val start = end.minusDays(days.toLong())
            repository.getDailySummary(start, end)
        }
}


