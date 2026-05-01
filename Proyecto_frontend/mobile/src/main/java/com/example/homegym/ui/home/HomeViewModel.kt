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

    private val _isGuest = MutableLiveData<Boolean>()
    val isGuest: LiveData<Boolean> = _isGuest

    init {
        viewModelScope.launch {
            com.example.homegym.util.TokenManager.isGuestMode(context).collect {
                _isGuest.value = it
            }
        }
    }

    fun fetchEjercicios() {
        viewModelScope.launch {
            _ejercicios.value = EjerciciosState.Loading
            
            // Esperar un momento a que isGuest se actualice si es necesario
            if (_isGuest.value == null) {
                val guest = com.example.homegym.util.TokenManager.isGuestMode(context).first()
                _isGuest.value = guest
            }

            try {
                val isGuest = _isGuest.value ?: false
                val token = if (!isGuest) {
                    com.example.homegym.util.TokenManager.getToken(context).first()
                } else {
                    null
                }
                
                // Si es invitado o tenemos token, intentamos cargar
                if (isGuest || token != null) {
                    val finalToken = token ?: ""
                    val response = repository.getEjercicios(finalToken)
                    if (response.isSuccessful) {
                        allEjercicios = response.body() ?: emptyList()
                        _ejercicios.value = EjerciciosState.Success(allEjercicios)
                        _filteredEjercicios.value = allEjercicios
                    } else {
                        // Si falla con 401 siendo invitado, el mensaje debe ser amigable
                        if (response.code() == 401 && isGuest) {
                            _ejercicios.value = EjerciciosState.Error("Inicia sesión para ver contenido personalizado")
                        } else {
                            _ejercicios.value = EjerciciosState.Error("Error: ${response.code()}")
                        }
                    }
                } else {
                    _ejercicios.value = EjerciciosState.Error("Inicia sesión o accede como invitado")
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
