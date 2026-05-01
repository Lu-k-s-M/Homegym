package com.example.homegym.data.api

import com.example.homegym.data.model.LoginRequest
import com.example.homegym.data.model.TokenResponse
import com.example.homegym.data.model.RegisterRequest
import com.example.homegym.data.model.UsuarioResponse
import com.example.homegym.data.model.PerfilUsuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Path

interface HomegymApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Unit>

    @GET("api/verificar-token")
    suspend fun verificarToken(
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @GET("api/ejercicios")
    suspend fun getEjercicios(
        @Header("Authorization") token: String
    ): Response<List<com.example.homegym.data.model.Ejercicio>>

    @GET("api/perfil")
    suspend fun getPerfil(
        @Header("Authorization") token: String
    ): Response<PerfilUsuario>

    @PUT("api/perfil")
    suspend fun updatePerfil(
        @Header("Authorization") token: String,
        @Body perfil: PerfilUsuario
    ): Response<PerfilUsuario>

    @GET("api/rutinas")
    suspend fun getRutinas(
        @Header("Authorization") token: String
    ): Response<List<com.example.homegym.data.model.Rutina>>

    @POST("api/rutinas")
    suspend fun crearRutina(
        @Header("Authorization") token: String,
        @Body request: com.example.homegym.data.model.RutinaCreateRequest
    ): Response<com.example.homegym.data.model.Rutina>

    @DELETE("api/rutinas/{id}")
    suspend fun eliminarRutina(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @POST("api/rutinas/multiples_delete")
    suspend fun eliminarMultiplesRutinas(
        @Header("Authorization") token: String,
        @Body ids: List<Int>
    ): Response<Unit>

    @DELETE("api/rutinas/limpiar")
    suspend fun limpiarRutinas(
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("api/historial")
    suspend fun getHistorial(
        @Header("Authorization") token: String
    ): Response<List<com.example.homegym.data.model.HistorialEjercicio>>

    @POST("api/historial/ejercicio/{ejercicioId}")
    suspend fun agregarAlHistorial(
        @Header("Authorization") token: String,
        @Path("ejercicioId") ejercicioId: Int
    ): Response<Unit>

    @DELETE("api/historial/{id}")
    suspend fun eliminarDelHistorial(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @POST("api/historial/multiples_delete") // Usamos POST para enviar el body en un DELETE si retrofit/servidor lo requieren o cambiamos a HTTP
    suspend fun eliminarMultiples(
        @Header("Authorization") token: String,
        @Body ids: List<Int>
    ): Response<Unit>

    @DELETE("api/historial/limpiar")
    suspend fun limpiarHistorial(
        @Header("Authorization") token: String
    ): Response<Unit>
}
