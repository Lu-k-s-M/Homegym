package com.example.homegym.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.homegym.ui.home.HomeActivity
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.model.PerfilUsuario
import com.example.homegym.data.repository.PerfilRepository
import com.example.homegym.databinding.FragmentPerfilBinding
import com.example.homegym.ui.home.HomeViewModel

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private var currentPerfil: PerfilUsuario? = null
    private lateinit var viewModel: PerfilViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = PerfilRepository(RetrofitClient.instance)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PerfilViewModel(repository, requireContext().applicationContext) as T
            }
        })[PerfilViewModel::class.java]

        setupListeners()
        observeViewModel()
        
        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        if (homeViewModel.isGuest.value != true) {
            viewModel.fetchPerfil()
        }
    }

    private fun setupListeners() {
        binding.btnGuardarPerfil.setOnClickListener {
            val perfil = PerfilUsuario(
                id = currentPerfil?.id,
                usuarioId = currentPerfil?.usuarioId,
                peso = binding.etPeso.text.toString().toDoubleOrNull(),
                altura = binding.etAltura.text.toString().toDoubleOrNull(),
                edad = binding.etEdad.text.toString().toIntOrNull(),
                objetivo = binding.etObjetivo.text.toString()
            )
            viewModel.updatePerfil(perfil)
        }
    }

    private fun observeViewModel() {
        val homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        homeViewModel.isGuest.observe(viewLifecycleOwner) { isGuest ->
            if (isGuest == true) {
                binding.btnGuardarPerfil.visibility = View.GONE
                binding.tilPeso.visibility = View.GONE
                binding.tilAltura.visibility = View.GONE
                binding.tilEdad.visibility = View.GONE
                binding.tilObjetivo.visibility = View.GONE
                binding.tvGuestMessage.visibility = View.VISIBLE
            } else {
                binding.btnGuardarPerfil.visibility = View.VISIBLE
                binding.tilPeso.visibility = View.VISIBLE
                binding.tilAltura.visibility = View.VISIBLE
                binding.tilEdad.visibility = View.VISIBLE
                binding.tilObjetivo.visibility = View.VISIBLE
                binding.tvGuestMessage.visibility = View.GONE
            }
        }

        viewModel.perfilState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PerfilState.Loading -> {
                    binding.pbPerfil.visibility = View.VISIBLE
                    binding.btnGuardarPerfil.isEnabled = false
                }
                is PerfilState.Success -> {
                    binding.pbPerfil.visibility = View.GONE
                    binding.btnGuardarPerfil.isEnabled = true
                    currentPerfil = state.perfil
                    populateFields(state.perfil)
                }
                is PerfilState.UpdateSuccess -> {
                    binding.pbPerfil.visibility = View.GONE
                    binding.btnGuardarPerfil.isEnabled = true
                    Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                }
                is PerfilState.Error -> {
                    binding.pbPerfil.visibility = View.GONE
                    binding.btnGuardarPerfil.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateFields(perfil: PerfilUsuario) {
        if (!binding.etPeso.hasFocus()) binding.etPeso.setText(perfil.peso?.toString() ?: "")
        if (!binding.etAltura.hasFocus()) binding.etAltura.setText(perfil.altura?.toString() ?: "")
        if (!binding.etEdad.hasFocus()) binding.etEdad.setText(perfil.edad?.toString() ?: "")
        if (!binding.etObjetivo.hasFocus()) binding.etObjetivo.setText(perfil.objetivo ?: "")
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
