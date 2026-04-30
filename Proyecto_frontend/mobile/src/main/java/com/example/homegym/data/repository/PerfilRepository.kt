package com.example.homegym.data.repository

import com.example.homegym.data.api.HomegymApiService
import com.example.homegym.data.model.PerfilUsuario
import retrofit2.Response

class PerfilRepository(private val apiService: HomegymApiService) {

    suspend fun getPerfil(token: String): Response<PerfilUsuario> {
        return apiService.getPerfil("Bearer $token")
    }

    suspend fun updatePerfil(token: String, perfil: PerfilUsuario): Response<PerfilUsuario> {
        return apiService.updatePerfil("Bearer $token", perfil)
    }
}
