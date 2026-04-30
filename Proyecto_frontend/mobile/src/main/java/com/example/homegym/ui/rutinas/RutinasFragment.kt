package com.example.homegym.ui.rutinas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homegym.ui.home.HomeActivity
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.repository.RutinaRepository
import com.example.homegym.databinding.FragmentRutinasBinding

class RutinasFragment : Fragment() {

    private var _binding: FragmentRutinasBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RutinasViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRutinasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = RutinaRepository(RetrofitClient.instance)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RutinasViewModel(repository, requireActivity().application) as T
            }
        })[RutinasViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        binding.fabAddRutina.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Crear rutina no implementado", android.widget.Toast.LENGTH_SHORT).show()
        }

        viewModel.fetchRutinas()
    }

    private fun setupRecyclerView() {
        binding.rvRutinas.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RutinasState.Loading -> {
                    binding.pbRutinas.visibility = View.VISIBLE
                    binding.rvRutinas.visibility = View.GONE
                    binding.tvErrorRutinas.visibility = View.GONE
                }
                is RutinasState.Success -> {
                    binding.pbRutinas.visibility = View.GONE
                    binding.rvRutinas.visibility = View.VISIBLE
                    binding.rvRutinas.adapter = RutinaAdapter(state.rutinas)
                }
                is RutinasState.Error -> {
                    binding.pbRutinas.visibility = View.GONE
                    binding.rvRutinas.visibility = View.GONE
                    binding.tvErrorRutinas.visibility = View.VISIBLE
                    binding.tvErrorRutinas.text = state.message
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.setSearchButtonVisibility(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
