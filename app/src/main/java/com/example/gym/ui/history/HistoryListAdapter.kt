package com.example.gym.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gym.data.DailySummaryRow
import com.example.gym.databinding.ItemHistoryBinding

class HistoryListAdapter : ListAdapter<DailySummaryRow, HistoryListAdapter.ViewHolder>(Diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object Diff : DiffUtil.ItemCallback<DailySummaryRow>() {
        override fun areItemsTheSame(oldItem: DailySummaryRow, newItem: DailySummaryRow): Boolean = oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: DailySummaryRow, newItem: DailySummaryRow): Boolean = oldItem == newItem
    }

    class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DailySummaryRow) {
            binding.date.text = item.date
            binding.progress.text = "${item.completedCount}/${item.totalCount}"
        }
    }
}


