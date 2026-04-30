package com.example.homegym.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class TokenResponse(
    val token: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null,
    val rol: String? = "user"
)

@Serializable
data class UsuarioResponse(
    val id: Int,
    val username: String,
    val rol: String
)

@Serializable
data class Ejercicio(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String,
    val intensidad: String,
    val parteCuerpo: String,
    val calorias: Int,
    val videoUrl: String? = null,
    val imagenUrl: String? = null
)
