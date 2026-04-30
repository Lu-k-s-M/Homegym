package com.homegym.models

data class Usuario(
    val id: Int? = null,
    val username: String,
    val password: String,
    val email: String? = null,
    val rol: RolUsuario
)
