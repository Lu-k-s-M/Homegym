package com.example.homegym.ui.rutinas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homegym.ui.home.HomeActivity
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.repository.RutinaRepository
import com.example.homegym.databinding.FragmentRutinasBinding

import androidx.appcompat.app.AlertDialog
import com.example.homegym.data.repository.EjercicioRepository
import com.example.homegym.databinding.DialogCreateRutinaBinding
import com.example.homegym.ui.home.PlaybackActivity
import com.example.homegym.ui.home.HomeViewModel

class RutinasFragment : Fragment() {

    private var _binding: FragmentRutinasBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RutinasViewModel
    private lateinit var adapter: RutinaAdapter

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
        val ejercicioRepository = EjercicioRepository(RetrofitClient.instance)
        
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RutinasViewModel(repository, ejercicioRepository, requireActivity().application) as T
            }
        })[RutinasViewModel::class.java]

        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.setSearchButtonVisibility(false)
        viewModel.fetchRutinas()
        viewModel.fetchEjercicios()
    }

    private fun setupRecyclerView() {
        adapter = RutinaAdapter(
            rutinas = emptyList(),
            onDeleteClick = { rutina ->
                rutina.id?.let { showDeleteConfirmation(listOf(it), "una rutina") }
            },
            onSelectionChanged = { updateSelectionButtons() },
            onItemClick = { rutina ->
                val intent = Intent(requireContext(), PlaybackActivity::class.java).apply {
                    if (rutina.ejercicios.isNotEmpty()) {
                        val ids = rutina.ejercicios.map { it.ejercicioId }.toIntArray()
                        putExtra("EJERCICIO_IDS", ids)
                        putExtra("VIDEO_TITLE", rutina.nombre)
                    }
                }
                startActivity(intent)
            }
        )
        binding.rvRutinas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRutinas.adapter = adapter
    }

    private fun setupButtons() {
        binding.fabAddRutina.setOnClickListener {
            showCreateRutinaDialog()
        }

        binding.btnClearAll.setOnClickListener {
            showDeleteConfirmation(null, "todas las rutinas")
        }

        binding.btnDeleteSelection.setOnClickListener {
            val ids = adapter.selectedIds.toList()
            if (ids.isNotEmpty()) {
                showDeleteConfirmation(ids, "${ids.size} rutinas")
            }
        }
    }

    private fun showDeleteConfirmation(ids: List<Int>?, messagePart: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar borrado")
            .setMessage("¿Estás seguro de que quieres borrar $messagePart?")
            .setPositiveButton("Borrar") { _, _ ->
                when {
                    ids == null -> viewModel.limpiarTodas()
                    ids.size == 1 -> viewModel.eliminarRutina(ids[0])
                    else -> viewModel.eliminarMultiples(ids)
                }
                adapter.selectedIds.clear()
                updateSelectionButtons()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateSelectionButtons() {
        binding.btnDeleteSelection.visibility = if (adapter.selectedIds.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun showCreateRutinaDialog() {
        val dialogBinding = DialogCreateRutinaBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        var selectedEjercicioIds = listOf<Int>()

        val adapter = EjercicioSeleccionAdapter(viewModel.ejercicios.value ?: emptyList()) { ids ->
            selectedEjercicioIds = ids
        }
        dialogBinding.rvEjerciciosSeleccion.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvEjerciciosSeleccion.adapter = adapter

        viewModel.ejercicios.observe(viewLifecycleOwner) { ejercicios ->
            adapter.updateData(ejercicios)
        }

        dialogBinding.btnGuardar.setOnClickListener {
            val nombre = dialogBinding.etNombre.text.toString().trim()
            val descripcion = dialogBinding.etDescripcion.text.toString().trim()
            val duracion = dialogBinding.etDuracion.text.toString().toIntOrNull() ?: 0
            val calorias = dialogBinding.etCalorias.text.toString().toIntOrNull() ?: 0

            if (nombre.isNotEmpty() && selectedEjercicioIds.isNotEmpty()) {
                viewModel.crearRutina(
                    nombre = nombre,
                    descripcion = descripcion,
                    duracion = duracion,
                    calorias = calorias,
                    ejerciciosIds = selectedEjercicioIds
                )
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Nombre y al menos un ejercicio requeridos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun observeViewModel() {
        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        homeViewModel.isGuest.observe(viewLifecycleOwner) { isGuest ->
            if (isGuest == true) {
                binding.fabAddRutina.visibility = View.GONE
                binding.btnClearAll.visibility = View.GONE
                binding.btnDeleteSelection.visibility = View.GONE
                binding.tvErrorRutinas.text = "Las rutinas no están disponibles para invitados. ¡Regístrate para crear las tuyas!"
                binding.tvErrorRutinas.visibility = View.VISIBLE
                binding.rvRutinas.visibility = View.GONE
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RutinasState.Loading -> {
                    binding.pbRutinas.visibility = View.VISIBLE
                    binding.rvRutinas.visibility = View.GONE
                    binding.tvErrorRutinas.visibility = View.GONE
                }
                is RutinasState.Success -> {
                    val isGuest = homeViewModel.isGuest.value ?: false
                    if (!isGuest) {
                        binding.pbRutinas.visibility = View.GONE
                        binding.rvRutinas.visibility = View.VISIBLE
                        binding.tvErrorRutinas.visibility = View.GONE
                        adapter.updateData(state.rutinas)
                    }
                }
                is RutinasState.Error -> {
                    val isGuest = homeViewModel.isGuest.value ?: false
                    if (!isGuest) {
                        binding.pbRutinas.visibility = View.GONE
                        binding.rvRutinas.visibility = View.GONE
                        binding.tvErrorRutinas.visibility = View.VISIBLE
                        binding.tvErrorRutinas.text = state.message
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
