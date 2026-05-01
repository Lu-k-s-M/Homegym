package com.example.homegym.ui.rutinas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homegym.data.model.Rutina
import com.example.homegym.data.repository.RutinaRepository
import com.example.homegym.util.TokenManager
import com.example.homegym.data.api.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.homegym.data.model.Ejercicio
import com.example.homegym.data.repository.EjercicioRepository

sealed class RutinasState {
    object Loading : RutinasState()
    data class Success(val rutinas: List<Rutina>) : RutinasState()
    data class Error(val message: String) : RutinasState()
}

class RutinasViewModel(
    private val repository: RutinaRepository,
    private val ejercicioRepository: EjercicioRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableLiveData<RutinasState>()
    val state: LiveData<RutinasState> = _state

    private val _ejercicios = MutableLiveData<List<Ejercicio>>()
    val ejercicios: LiveData<List<Ejercicio>> = _ejercicios

    fun fetchRutinas() {
        _state.value = RutinasState.Loading
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(getApplication()).first()
                if (token != null) {
                    val response = repository.getRutinas(token)
                    if (response.isSuccessful) {
                        _state.value = RutinasState.Success(response.body() ?: emptyList())
                    } else {
                        _state.value = RutinasState.Error("Error al cargar rutinas: ${response.code()}")
                    }
                } else {
                    _state.value = RutinasState.Error("Inicia sesión o regístrate para gestionar tus propias rutinas")
                }
            } catch (e: Exception) {
                _state.value = RutinasState.Error("Excepción: ${e.message}")
            }
        }
    }

    fun fetchEjercicios() {
        viewModelScope.launch {
            try {
                // Realizamos la petición sin token o con token vacío, ya que el endpoint es público
                val response = ejercicioRepository.getEjercicios("")
                if (response.isSuccessful) {
                    _ejercicios.value = response.body() ?: emptyList()
                    android.util.Log.d("RutinasViewModel", "Ejercicios cargados: ${response.body()?.size}")
                } else {
                    android.util.Log.e("RutinasViewModel", "Error al cargar ejercicios: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("RutinasViewModel", "Excepción al cargar ejercicios: ${e.message}")
            }
        }
    }

    fun crearRutina(nombre: String, descripcion: String, duracion: Int, calorias: Int, ejerciciosIds: List<Int>) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(getApplication()).first()
                if (token != null) {
                    val perfilResponse = RetrofitClient.instance.getPerfil("Bearer $token")
                    val usuarioId = perfilResponse.body()?.usuarioId ?: 0
                    
                    val request = com.example.homegym.data.model.RutinaCreateRequest(
                        usuarioId = usuarioId,
                        nombre = nombre,
                        descripcion = if (descripcion.isEmpty()) null else descripcion,
                        duracionMinutos = duracion,
                        calorias = calorias,
                        ejercicios = ejerciciosIds.map { id ->
                            com.example.homegym.data.model.EjercicioRutinaCreateRequest(
                                ejercicioId = id,
                                series = 3,
                                repeticiones = 10,
                                descansoSegundos = 60
                            )
                        }
                    )
                    
                    val response = repository.crearRutina(token, request)
                    if (response.isSuccessful) {
                        fetchRutinas()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: ""
                        android.util.Log.e("RutinasViewModel", "Error ${response.code()}: $errorBody")
                        _state.value = RutinasState.Error("Error ${response.code()}: $errorBody")
                    }
                }
            } catch (e: Exception) {
                _state.value = RutinasState.Error("Excepción: ${e.message}")
            }
        }
    }

    fun eliminarRutina(id: Int) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(getApplication()).first()
                if (token != null) {
                    val response = repository.eliminarRutina(token, id)
                    if (response.isSuccessful) {
                        fetchRutinas()
                    }
                }
            } catch (e: Exception) {
                _state.value = RutinasState.Error("Error al eliminar: ${e.message}")
            }
        }
    }

    fun eliminarMultiples(ids: List<Int>) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(getApplication()).first()
                if (token != null) {
                    val response = repository.eliminarMultiples(token, ids)
                    if (response.isSuccessful) {
                        fetchRutinas()
                    }
                }
            } catch (e: Exception) {
                _state.value = RutinasState.Error("Error al eliminar: ${e.message}")
            }
        }
    }

    fun limpiarTodas() {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(getApplication()).first()
                if (token != null) {
                    val response = repository.limpiarTodas(token)
                    if (response.isSuccessful) {
                        fetchRutinas()
                    }
                }
            } catch (e: Exception) {
                _state.value = RutinasState.Error("Error al limpiar: ${e.message}")
            }
        }
    }
}
