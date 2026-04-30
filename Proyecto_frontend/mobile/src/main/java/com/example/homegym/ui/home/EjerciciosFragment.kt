package com.example.homegym.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.repository.EjercicioRepository
import com.example.homegym.data.repository.HistorialRepository
import com.example.homegym.databinding.FragmentEjerciciosBinding

class EjerciciosFragment : Fragment() {

    private var _binding: FragmentEjerciciosBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var recientesViewModel: RecientesViewModel
    private lateinit var historialAdapter: HistorialHorizontalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEjerciciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = EjercicioRepository(RetrofitClient.instance)
        val historialRepository = HistorialRepository(requireContext().applicationContext, RetrofitClient.instance)

        viewModel = ViewModelProvider(requireActivity(), object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(repository, requireContext().applicationContext) as T
            }
        })[HomeViewModel::class.java]

        recientesViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecientesViewModel(historialRepository) as T
            }
        })[RecientesViewModel::class.java]

        setupRecyclerViews()
        observeViewModel()

        viewModel.fetchEjercicios()
    }
    
    override fun onResume() {
        super.onResume()
        recientesViewModel.cargarHistorial()
        (activity as? HomeActivity)?.setSearchButtonVisibility(true)
    }

    private fun setupRecyclerViews() {
        historialAdapter = HistorialHorizontalAdapter(emptyList())
        binding.rvRecientes.adapter = historialAdapter
    }

    private fun observeViewModel() {
        viewModel.ejercicios.observe(viewLifecycleOwner) { state ->
            when (state) {
                is EjerciciosState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is EjerciciosState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    setupHorizontalLists(state.ejercicios)
                }
                is EjerciciosState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        recientesViewModel.historial.observe(viewLifecycleOwner) { historial ->
            if (historial.isNotEmpty()) {
                binding.tvRecientesHeader.visibility = View.VISIBLE
                binding.rvRecientes.visibility = View.VISIBLE
                historialAdapter.updateData(historial)
            } else {
                binding.tvRecientesHeader.visibility = View.GONE
                binding.rvRecientes.visibility = View.GONE
            }
        }
    }

    private fun setupHorizontalLists(ejercicios: List<com.example.homegym.data.model.Ejercicio>) {
        if (ejercicios.isEmpty()) {
            binding.rvTendencias.adapter = EjercicioHorizontalAdapter(emptyList())
            binding.rvRecomendados.adapter = EjercicioHorizontalAdapter(emptyList())
            binding.rvParteSuperior.adapter = EjercicioHorizontalAdapter(emptyList())
            
            binding.tvTendenciasHeader.visibility = View.GONE
            binding.rvTendencias.visibility = View.GONE
            binding.tvParaTiHeader.visibility = View.GONE
            binding.rvRecomendados.visibility = View.GONE
            binding.tvParteSuperiorHeader.visibility = View.GONE
            binding.rvParteSuperior.visibility = View.GONE
            return
        }

        binding.tvTendenciasHeader.visibility = View.VISIBLE
        binding.rvTendencias.visibility = View.VISIBLE
        binding.tvParaTiHeader.visibility = View.VISIBLE
        binding.rvRecomendados.visibility = View.VISIBLE
        binding.tvParteSuperiorHeader.visibility = View.VISIBLE
        binding.rvParteSuperior.visibility = View.VISIBLE

        val tendencias = ejercicios.shuffled().take(5)
        val recomendados = ejercicios.shuffled().take(5)
        val parteSuperior = ejercicios.filter { it.parteCuerpo.contains("Brazo", ignoreCase = true) || it.parteCuerpo.contains("Pecho", ignoreCase = true) }

        binding.rvTendencias.adapter = EjercicioHorizontalAdapter(tendencias)
        binding.rvRecomendados.adapter = EjercicioHorizontalAdapter(recomendados)
        binding.rvParteSuperior.adapter = EjercicioHorizontalAdapter(if (parteSuperior.isNotEmpty()) parteSuperior else ejercicios.take(5))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
