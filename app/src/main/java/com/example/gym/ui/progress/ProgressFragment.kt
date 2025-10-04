package com.example.gym.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym.databinding.FragmentProgressBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.util.Calendar

class ProgressFragment : Fragment() {
    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels()
    private lateinit var adapter: ProgressListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProgressListAdapter(onToggle = { id, checked ->
            viewModel.toggle(id, checked)
        })
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        for (day in days) {
            binding.dayTabs.addTab(binding.dayTabs.newTab().setText(day))
        }

        binding.dayTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val day = tab?.position?.plus(1) ?: 1
                viewModel.selectDay(day)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Auto-select today's weekday (Mon=1..Sun=7)
        val today = Calendar.getInstance()
        // Calendar.DAY_OF_WEEK is Sun=1..Sat=7, we want Mon=1..Sun=7
        val todayDow = when (today.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 7
            else -> today.get(Calendar.DAY_OF_WEEK) - 1
        }
        binding.dayTabs.getTabAt(todayDow - 1)?.select()
        viewModel.selectDay(todayDow)

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submit(state.exercises, state.progressByExerciseId, state.dayOfWeek)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
