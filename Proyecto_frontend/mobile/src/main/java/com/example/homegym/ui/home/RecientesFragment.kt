package com.example.homegym.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.repository.HistorialRepository
import com.example.homegym.databinding.FragmentRecientesBinding

class RecientesFragment : Fragment() {

    private var _binding: FragmentRecientesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecientesViewModel
    private lateinit var adapter: HistorialAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = HistorialRepository(requireContext(), RetrofitClient.instance)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecientesViewModel(repository) as T
            }
        })[RecientesViewModel::class.java]

        setupRecyclerView()
        setupButtons()
        observeViewModel()

        viewModel.cargarHistorial()
    }

    private fun setupRecyclerView() {
        adapter = HistorialAdapter(
            items = emptyList(),
            onDeleteClick = { item -> 
                item.id?.let { showDeleteConfirmation(listOf(it), "este registro") } 
            },
            onSelectionChanged = { updateSelectionButtons() },
            onItemClick = { item ->
                val intent = Intent(requireContext(), PlaybackActivity::class.java).apply {
                    putExtra("VIDEO_URL", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                    putExtra("VIDEO_TITLE", item.ejercicioNombre)
                    putExtra("EJERCICIO_ID", item.ejercicioId)
                }
                startActivity(intent)
            }
        )
        binding.rvRecientes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecientes.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnClearAll.setOnClickListener {
            showDeleteConfirmation(null, "todo el historial")
        }

        binding.btnDeleteSelection.setOnClickListener {
            val ids = adapter.selectedIds.toList()
            if (ids.isNotEmpty()) {
                showDeleteConfirmation(ids, "${ids.size} registros")
            }
        }
    }

    private fun showDeleteConfirmation(ids: List<Int>?, messagePart: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar borrado")
            .setMessage("¿Estás seguro de que quieres borrar $messagePart?")
            .setPositiveButton("Borrar") { _, _ ->
                when {
                    ids == null -> viewModel.limpiarTodo()
                    ids.size == 1 -> viewModel.eliminarUno(ids[0])
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

    private fun observeViewModel() {
        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        homeViewModel.isGuest.observe(viewLifecycleOwner) { isGuest ->
            if (isGuest == true) {
                binding.btnClearAll.visibility = View.GONE
                binding.btnDeleteSelection.visibility = View.GONE
                binding.tvEmpty.text = "Historial no disponible para invitados. ¡Regístrate para guardar tu progreso!"
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvRecientes.visibility = View.GONE
            }
        }

        viewModel.historial.observe(viewLifecycleOwner) { items ->
            val isGuest = homeViewModel.isGuest.value ?: false
            if (!isGuest) {
                adapter.updateData(items)
                binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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
