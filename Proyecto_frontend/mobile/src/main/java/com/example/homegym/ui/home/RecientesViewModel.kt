package com.example.homegym.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homegym.data.model.HistorialEjercicio
import com.example.homegym.data.repository.HistorialRepository
import kotlinx.coroutines.launch

class RecientesViewModel(private val repository: HistorialRepository) : ViewModel() {

    private val _historial = MutableLiveData<List<HistorialEjercicio>>()
    val historial: LiveData<List<HistorialEjercicio>> = _historial

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun cargarHistorial() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getHistorial()
                if (response.isSuccessful) {
                    _historial.value = response.body() ?: emptyList()
                    _error.value = null
                } else {
                    _error.value = "Error al cargar el historial"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarUno(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.eliminarDelHistorial(id)
                if (response.isSuccessful) {
                    cargarHistorial()
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar"
            }
        }
    }

    fun eliminarMultiples(ids: List<Int>) {
        viewModelScope.launch {
            try {
                val response = repository.eliminarMultiples(ids)
                if (response.isSuccessful) {
                    cargarHistorial()
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar seleccionados"
            }
        }
    }

    fun limpiarTodo() {
        viewModelScope.launch {
            try {
                val response = repository.limpiarHistorial()
                if (response.isSuccessful) {
                    _historial.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Error al limpiar el historial"
            }
        }
    }
}
