package com.example.gym.ui.progress

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym.databinding.FragmentProgressBinding
import kotlinx.coroutines.launch

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProgressListAdapter(onToggle = { id, checked ->
            viewModel.toggle(id, checked)
        })
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.dayTabs.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val day = when (checkedId) {
                binding.btnMon.id -> 1
                binding.btnTue.id -> 2
                binding.btnWed.id -> 3
                binding.btnThu.id -> 4
                binding.btnFri.id -> 5
                binding.btnSat.id -> 6
                binding.btnSun.id -> 7
                else -> 1
            }
            viewModel.selectDay(day)
            // Auto-scroll selected button into view
            val selected = group.findViewById<View>(checkedId)
            binding.dayTabsScroll.post { binding.dayTabsScroll.smoothScrollTo(selected.left, 0) }
        }

        // Auto-select today's weekday (Mon=1..Sun=7)
        val todayDow = java.time.LocalDate.now().dayOfWeek.value // Mon=1..Sun=7
        val initialDay = when (todayDow) {
            1 -> binding.btnMon.isChecked = true
            2 -> binding.btnTue.isChecked = true
            3 -> binding.btnWed.isChecked = true
            4 -> binding.btnThu.isChecked = true
            5 -> binding.btnFri.isChecked = true
            6 -> binding.btnSat.isChecked = true
            7 -> binding.btnSun.isChecked = true
            else -> binding.btnMon.isChecked = true
        }.let { when (todayDow) { 1->1; 2->2; 3->3; 4->4; 5->5; 6->6; 7->7; else->1 } }
        // Ensure initial state loads even if listener didn't trigger
        viewModel.selectDay(initialDay)

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


