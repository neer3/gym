package com.example.gym.ui.progress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gym.data.Exercise
import com.example.gym.data.ExerciseProgress
import com.example.gym.databinding.ItemExerciseBinding

class ProgressListAdapter(
    private val onToggle: (Long, Boolean) -> Unit
) : ListAdapter<Exercise, ProgressListAdapter.ViewHolder>(Diff) {

    private var progressById: Map<Long, ExerciseProgress> = emptyMap()
    private val expandedIds: MutableSet<Long> = linkedSetOf()
    private var dayOfWeek: Int = 1 // Default to Monday

    fun submit(exercises: List<Exercise>, progress: Map<Long, ExerciseProgress>, dayOfWeek: Int = 1) {
        progressById = progress
        this.dayOfWeek = dayOfWeek
        submitList(exercises)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val expanded = expandedIds.contains(item.id)
        holder.bind(item, progressById[item.id]?.completed == true, expanded, dayOfWeek, onToggle) { toggled ->
            if (toggled) expandedIds.add(item.id) else expandedIds.remove(item.id)
        }
    }

    object Diff : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean = oldItem == newItem
    }

    class ViewHolder(private val binding: ItemExerciseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Exercise,
            checked: Boolean,
            expanded: Boolean,
            dayOfWeek: Int,
            onToggle: (Long, Boolean) -> Unit,
            onExpandToggle: (Boolean) -> Unit
        ) {
            binding.exerciseName.text = item.exerciseName
            binding.check.setOnCheckedChangeListener(null)
            binding.check.isChecked = checked
            binding.check.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item.id, isChecked)
            }

            val detailsGroup = listOf(binding.focus, binding.sets, binding.muscles, binding.equipment, binding.alternative, binding.intensity, binding.time)
            detailsGroup.forEach { it.visibility = if (expanded) View.VISIBLE else View.GONE }

            binding.root.setOnClickListener {
                val nowExpanded = detailsGroup.first().visibility != View.VISIBLE
                detailsGroup.forEach { it.visibility = if (nowExpanded) View.VISIBLE else View.GONE }
                onExpandToggle(nowExpanded)
            }

            // Show appropriate focus field based on day of week
            val focusText = when (dayOfWeek) {
                1 -> item.backFatFocus?.let { "Focus: $it" }
                2 -> item.chestDevelopmentFocus?.let { "Focus: $it" }
                3 -> item.calorieBurnFocus?.let { "Focus: $it" }
                4 -> item.vTaperCoreFocus?.let { "Focus: $it" }
                5 -> item.definitionFocus?.let { "Focus: $it" }
                6 -> item.fatBurnStrategy?.let { "Focus: $it" }
                7 -> item.homeWorkoutBenefits?.let { "Focus: $it" }
                else -> item.backFatFocus?.let { "Focus: $it" }
            }
            binding.focus.text = focusText ?: ""
            binding.sets.text = "Sets: ${item.sets} × ${item.reps}"
            binding.muscles.text = "Muscles: ${item.primaryMuscleTarget}"
            binding.equipment.text = item.machineSetup?.let { "Setup: $it" } ?: ""
            // Show appropriate alternative field based on day of week
            val alternativeText = when (dayOfWeek) {
                7 -> item.machineAlternative?.let { "Bodyweight Alternative: $it" } // Sunday uses bodyweight alternatives
                else -> item.machineAlternative?.let { "Alternative: $it" } // Other days use machine alternatives
            }
            binding.alternative.text = alternativeText ?: ""
            // Show appropriate intensity field based on day of week
            val intensityText = when (dayOfWeek) {
                7 -> item.weightRangeKg?.let { "Resistance: $it" } // Sunday uses band resistance
                else -> item.weightRangeKg?.let { "Weight: ${it}kg" } // Other days use weight
            }
            binding.intensity.text = intensityText ?: ""
            binding.time.text = item.restTimeSeconds?.let { "Rest: $it" } ?: ""
        }
    }
}
