package com.example.homegym.data.repository

import com.example.homegym.data.api.HomegymApiService
import com.example.homegym.data.model.Ejercicio
import retrofit2.Response

class EjercicioRepository(private val apiService: HomegymApiService) {
    suspend fun getEjercicios(token: String): Response<List<Ejercicio>> {
        return apiService.getEjercicios("Bearer $token")
    }
}
