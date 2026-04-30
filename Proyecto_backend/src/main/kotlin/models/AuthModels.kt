package com.homegym.models

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
data class ChangeRoleRequest(
    val rol: String
)

@Serializable
data class UsuarioResponse(
    val id: Int,
    val username: String,
    val rol: String
)

@Serializable
data class UsuariosPaginadosResponse(
    val page: Int,
    val size: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<UsuarioResponse>
)
