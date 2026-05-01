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
        if (viewModel.isGuest.value != true) {
            recientesViewModel.cargarHistorial()
        }
        (activity as? HomeActivity)?.setSearchButtonVisibility(true)
    }

    private fun setupRecyclerViews() {
        historialAdapter = HistorialHorizontalAdapter(emptyList())
        binding.rvRecientes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecientes.adapter = historialAdapter

        binding.rvTendencias.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecomendados.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvParteSuperior.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPiernaGluteo.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCardio.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
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
            binding.rvPiernaGluteo.adapter = EjercicioHorizontalAdapter(emptyList())
            binding.rvCardio.adapter = EjercicioHorizontalAdapter(emptyList())
            
            binding.tvTendenciasHeader.visibility = View.GONE
            binding.rvTendencias.visibility = View.GONE
            binding.tvParaTiHeader.visibility = View.GONE
            binding.rvRecomendados.visibility = View.GONE
            binding.tvParteSuperiorHeader.visibility = View.GONE
            binding.rvParteSuperior.visibility = View.GONE
            binding.tvPiernaGluteoHeader.visibility = View.GONE
            binding.rvPiernaGluteo.visibility = View.GONE
            binding.tvCardioHeader.visibility = View.GONE
            binding.rvCardio.visibility = View.GONE
            return
        }

        binding.tvTendenciasHeader.visibility = View.VISIBLE
        binding.rvTendencias.visibility = View.VISIBLE
        binding.tvParaTiHeader.visibility = View.VISIBLE
        binding.rvRecomendados.visibility = View.VISIBLE
        binding.tvParteSuperiorHeader.visibility = View.VISIBLE
        binding.rvParteSuperior.visibility = View.VISIBLE
        binding.tvPiernaGluteoHeader.visibility = View.VISIBLE
        binding.rvPiernaGluteo.visibility = View.VISIBLE
        binding.tvCardioHeader.visibility = View.VISIBLE
        binding.rvCardio.visibility = View.VISIBLE

        val tendencias = ejercicios.shuffled().take(6)
        val recomendados = ejercicios.shuffled().take(6)
        
        val parteSuperior = ejercicios.filter { 
            it.parteCuerpo.contains("Pecho", ignoreCase = true) || 
            it.parteCuerpo.contains("Brazo", ignoreCase = true) || 
            it.parteCuerpo.contains("Espalda", ignoreCase = true) ||
            it.parteCuerpo.contains("Tríceps", ignoreCase = true) ||
            it.parteCuerpo.contains("Bíceps", ignoreCase = true)
        }
        
        val piernaGluteo = ejercicios.filter { 
            it.parteCuerpo.contains("Pierna", ignoreCase = true) || 
            it.parteCuerpo.contains("Glúteo", ignoreCase = true) 
        }
        
        val cardio = ejercicios.filter { 
            it.parteCuerpo.contains("Cardio", ignoreCase = true) || 
            it.descripcion.contains("aeróbico", ignoreCase = true) ||
            it.intensidad.equals("Alta", ignoreCase = true)
        }

        binding.rvTendencias.adapter = EjercicioHorizontalAdapter(tendencias)
        binding.rvRecomendados.adapter = EjercicioHorizontalAdapter(recomendados)
        binding.rvParteSuperior.adapter = EjercicioHorizontalAdapter(if (parteSuperior.isNotEmpty()) parteSuperior else ejercicios.take(5))
        binding.rvPiernaGluteo.adapter = EjercicioHorizontalAdapter(if (piernaGluteo.isNotEmpty()) piernaGluteo else ejercicios.take(5))
        binding.rvCardio.adapter = EjercicioHorizontalAdapter(if (cardio.isNotEmpty()) cardio else ejercicios.take(5))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
