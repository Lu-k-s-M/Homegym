package com.example.homegym.data.repository

import com.example.homegym.data.api.HomegymApiService
import com.example.homegym.data.model.LoginRequest
import com.example.homegym.data.model.RegisterRequest
import com.example.homegym.data.model.TokenResponse
import retrofit2.Response

class AuthRepository(private val apiService: HomegymApiService) {
    suspend fun login(loginRequest: LoginRequest): Response<TokenResponse> {
        return apiService.login(loginRequest)
    }

    suspend fun register(registerRequest: RegisterRequest): Response<Unit> {
        return apiService.register(registerRequest)
    }

    suspend fun verificarToken(token: String): Response<Map<String, String>> {
        return apiService.verificarToken("Bearer $token")
    }
}
