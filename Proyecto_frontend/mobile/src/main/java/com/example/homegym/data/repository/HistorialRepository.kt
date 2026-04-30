package com.example.homegym.data.repository

import com.example.homegym.data.api.HomegymApiService
import com.example.homegym.data.model.HistorialEjercicio
import com.example.homegym.util.TokenManager
import kotlinx.coroutines.flow.first
import retrofit2.Response

class HistorialRepository(
    private val context: android.content.Context,
    private val apiService: HomegymApiService
) {
    private suspend fun getToken(): String {
        return "Bearer ${com.example.homegym.util.TokenManager.getToken(context).first() ?: ""}"
    }

    suspend fun getHistorial(): Response<List<HistorialEjercicio>> {
        return apiService.getHistorial(getToken())
    }

    suspend fun agregarAlHistorial(ejercicioId: Int): Response<Unit> {
        return apiService.agregarAlHistorial(getToken(), ejercicioId)
    }

    suspend fun eliminarDelHistorial(id: Int): Response<Unit> {
        return apiService.eliminarDelHistorial(getToken(), id)
    }

    suspend fun eliminarMultiples(ids: List<Int>): Response<Unit> {
        return apiService.eliminarMultiples(getToken(), ids)
    }

    suspend fun limpiarHistorial(): Response<Unit> {
        return apiService.limpiarHistorial(getToken())
    }
}
