package com.example.gym.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gym.R
import com.example.gym.data.Exercise

class ExerciseListAdapter(
    private val onExerciseClick: (Exercise) -> Unit
) : ListAdapter<Exercise, ExerciseListAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view, onExerciseClick)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExerciseViewHolder(
        itemView: View,
        private val onExerciseClick: (Exercise) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val exerciseOrder: TextView = itemView.findViewById(R.id.exerciseOrder)
        private val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        private val primaryMuscleTarget: TextView = itemView.findViewById(R.id.primaryMuscleTarget)
        private val setsReps: TextView = itemView.findViewById(R.id.setsReps)
        private val weightRange: TextView = itemView.findViewById(R.id.weightRange)

        fun bind(exercise: Exercise) {
            exerciseOrder.text = exercise.exerciseOrder.ifEmpty { "N/A" }
            exerciseName.text = exercise.exerciseName
            primaryMuscleTarget.text = exercise.primaryMuscleTarget
            setsReps.text = "${exercise.sets} sets × ${exercise.reps}"
            weightRange.text = "${exercise.weightRangeKg} kg"
            
            // Debug log to verify exercise order is being set
            android.util.Log.d("ExerciseAdapter", "Binding exercise: ${exercise.exerciseOrder} - ${exercise.exerciseName}")
            
            itemView.setOnClickListener {
                onExerciseClick(exercise)
            }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}
