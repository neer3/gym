package com.example.gym.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gym.R
import com.example.gym.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var exerciseAdapter: ExerciseListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        observeViewModel()
        
        return root
    }
    
    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseListAdapter { exercise ->
            // Navigate to exercise detail
            val bundle = bundleOf("exerciseId" to exercise.id)
            findNavController().navigate(R.id.action_nav_home_to_exerciseDetailFragment, bundle)
        }
        
        binding.exerciseRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = exerciseAdapter
        }
    }
    
    private fun observeViewModel() {
        homeViewModel.exercises.observe(viewLifecycleOwner) { exercises ->
            exerciseAdapter.submitList(exercises)
        }
        
        homeViewModel.currentDayName.observe(viewLifecycleOwner) { dayName ->
            homeViewModel.workoutFocus.observe(viewLifecycleOwner) { focus ->
                binding.dayTitle.text = "$dayName Workout - $focus"
            }
        }
        
        homeViewModel.isRestDay.observe(viewLifecycleOwner) { isRestDay ->
            if (isRestDay) {
                binding.dayTitle.text = "Sunday - Rest Day"
                binding.exerciseRecyclerView.visibility = View.GONE
                binding.restDayMessage.visibility = View.VISIBLE
            } else {
                binding.exerciseRecyclerView.visibility = View.VISIBLE
                binding.restDayMessage.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}