package com.example.homegym.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.homegym.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        viewModel.filteredEjercicios.observe(viewLifecycleOwner) { ejercicios ->
            if (ejercicios.isEmpty()) {
                binding.tvNoResults.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.tvNoResults.visibility = View.GONE
                binding.rvSearchResults.visibility = View.VISIBLE
                binding.rvSearchResults.adapter = EjercicioAdapter(ejercicios)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.setSearchButtonVisibility(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}