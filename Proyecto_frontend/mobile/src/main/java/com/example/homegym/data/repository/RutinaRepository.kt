package com.example.homegym.data.repository

import com.example.homegym.data.api.HomegymApiService
import com.example.homegym.data.model.Rutina
import retrofit2.Response

class RutinaRepository(private val apiService: HomegymApiService) {

    suspend fun getRutinas(token: String): Response<List<Rutina>> {
        return apiService.getRutinas("Bearer $token")
    }
}
