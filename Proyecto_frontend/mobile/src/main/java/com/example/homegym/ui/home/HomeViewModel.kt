package com.example.homegym.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homegym.data.model.Ejercicio
import com.example.homegym.data.repository.EjercicioRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class EjerciciosState {
    object Loading : EjerciciosState()
    data class Success(val ejercicios: List<Ejercicio>) : EjerciciosState()
    data class Error(val message: String) : EjerciciosState()
}

class HomeViewModel(
    private val repository: EjercicioRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val _ejercicios = MutableLiveData<EjerciciosState>()
    val ejercicios: LiveData<EjerciciosState> = _ejercicios

    private var allEjercicios: List<Ejercicio> = emptyList()

    private val _filteredEjercicios = MutableLiveData<List<Ejercicio>>()
    val filteredEjercicios: LiveData<List<Ejercicio>> = _filteredEjercicios

    fun fetchEjercicios() {
        viewModelScope.launch {
            _ejercicios.value = EjerciciosState.Loading
            try {
                val token = com.example.homegym.util.TokenManager.getToken(context).first()
                if (token != null) {
                    val response = repository.getEjercicios(token)
                    if (response.isSuccessful) {
                        allEjercicios = response.body() ?: emptyList()
                        _ejercicios.value = EjerciciosState.Success(allEjercicios)
                        _filteredEjercicios.value = allEjercicios
                    } else {
                        _ejercicios.value = EjerciciosState.Error("Error: ${response.code()}")
                    }
                } else {
                    _ejercicios.value = EjerciciosState.Error("No hay token")
                }
            } catch (e: Exception) {
                _ejercicios.value = EjerciciosState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun filterEjercicios(query: String) {
        if (query.isEmpty()) {
            _filteredEjercicios.value = allEjercicios
        } else {
            val filtered = allEjercicios.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                        it.descripcion.contains(query, ignoreCase = true) ||
                        it.parteCuerpo.contains(query, ignoreCase = true)
            }
            _filteredEjercicios.value = filtered
        }
    }
}
