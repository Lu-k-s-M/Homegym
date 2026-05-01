package com.example.homegym.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homegym.data.model.LoginRequest
import com.example.homegym.data.model.RegisterRequest
import com.example.homegym.data.repository.AuthRepository
import com.example.homegym.util.TokenManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginState>()
    val loginResult: LiveData<LoginState> = _loginResult

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginResult.value = LoginState.Error("Usuario y contraseña son obligatorios")
            return
        }

        _loginResult.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = repository.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        TokenManager.saveToken(context, token)
                        _loginResult.value = LoginState.Success
                    } else {
                        _loginResult.value = LoginState.Error("Token no recibido")
                    }
                } else {
                    _loginResult.value = LoginState.Error("Credenciales inválidas")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    fun enterAsGuest() {
        viewModelScope.launch {
            TokenManager.setGuestMode(context, true)
            _loginResult.value = LoginState.GuestSuccess
        }
    }

    fun register(username: String, password: String, email: String) {
        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            _loginResult.value = LoginState.Error("Todos los campos son obligatorios")
            return
        }

        _loginResult.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = repository.register(RegisterRequest(username, password, email))
                if (response.isSuccessful) {
                    _loginResult.value = LoginState.RegisterSuccess
                } else {
                    val errorBody = response.errorBody()?.string()
                    _loginResult.value = LoginState.Error("Error en el registro: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _loginResult.value = LoginState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    object RegisterSuccess : LoginState()
    data class Error(val message: String) : LoginState()
    object GuestSuccess : LoginState()
}
