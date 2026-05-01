package com.example.homegym.ui.perfil

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homegym.data.model.PerfilUsuario
import com.example.homegym.data.repository.PerfilRepository
import com.example.homegym.util.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class PerfilState {
    object Loading : PerfilState()
    data class Success(val perfil: PerfilUsuario) : PerfilState()
    data class Error(val message: String) : PerfilState()
    object UpdateSuccess : PerfilState()
}

class PerfilViewModel(
    private val repository: PerfilRepository,
    private val context: Context
) : ViewModel() {

    private val _perfilState = MutableLiveData<PerfilState>()
    val perfilState: LiveData<PerfilState> = _perfilState

    fun fetchPerfil() {
        _perfilState.value = PerfilState.Loading
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(context).first()
                val isGuest = TokenManager.isGuestMode(context).first()
                
                if (isGuest) {
                    _perfilState.value = PerfilState.Success(PerfilUsuario(objetivo = "Modo Invitado"))
                    return@launch
                }

                if (token != null) {
                    val response = repository.getPerfil(token)
                    if (response.isSuccessful) {
                        _perfilState.value = PerfilState.Success(response.body() ?: PerfilUsuario())
                    } else {
                        _perfilState.value = PerfilState.Error("Error al obtener perfil: ${response.code()}")
                    }
                } else {
                    _perfilState.value = PerfilState.Error("No hay token disponible")
                }
            } catch (e: Exception) {
                _perfilState.value = PerfilState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun updatePerfil(perfil: PerfilUsuario) {
        _perfilState.value = PerfilState.Loading
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken(context).first()
                if (token != null) {
                    val response = repository.updatePerfil(token, perfil)
                    if (response.isSuccessful) {
                        _perfilState.value = PerfilState.UpdateSuccess
                    } else {
                        _perfilState.value = PerfilState.Error("Error al actualizar perfil: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _perfilState.value = PerfilState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
