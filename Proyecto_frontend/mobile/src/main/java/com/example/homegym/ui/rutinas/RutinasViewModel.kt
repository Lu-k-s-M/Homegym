package com.example.homegym.ui.rutinas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homegym.data.model.Rutina
import com.example.homegym.data.repository.RutinaRepository
import com.example.homegym.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class RutinasState {
    object Loading : RutinasState()
    data class Success(val rutinas: List<Rutina>) : RutinasState()
    data class Error(val message: String) : RutinasState()
}

class RutinasViewModel(
    private val repository: RutinaRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableLiveData<RutinasState>()
    val state: LiveData<RutinasState> = _state

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
                    _state.value = RutinasState.Error("No se encontró el token de sesión")
                }
            } catch (e: Exception) {
                _state.value = RutinasState.Error("Excepción: ${e.message}")
            }
        }
    }
}
