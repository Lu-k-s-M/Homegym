package com.example.homegym.data.repository

import com.example.homegym.data.api.HomegymApiService
import com.example.homegym.data.model.Rutina
import retrofit2.Response

class RutinaRepository(private val apiService: HomegymApiService) {

    suspend fun getRutinas(token: String): Response<List<Rutina>> {
        return apiService.getRutinas("Bearer $token")
    }

    suspend fun crearRutina(token: String, request: com.example.homegym.data.model.RutinaCreateRequest): Response<com.example.homegym.data.model.Rutina> {
        return apiService.crearRutina("Bearer $token", request)
    }

    suspend fun eliminarRutina(token: String, id: Int): Response<Unit> {
        return apiService.eliminarRutina("Bearer $token", id)
    }

    suspend fun eliminarMultiples(token: String, ids: List<Int>): Response<Unit> {
        return apiService.eliminarMultiplesRutinas("Bearer $token", ids)
    }

    suspend fun limpiarTodas(token: String): Response<Unit> {
        return apiService.limpiarRutinas("Bearer $token")
    }
}
