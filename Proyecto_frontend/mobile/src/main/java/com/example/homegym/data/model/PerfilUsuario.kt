package com.example.homegym.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PerfilUsuario(
    val id: Int? = null,
    val usuarioId: Int? = 0,
    val peso: Double? = null,
    val altura: Double? = null,
    val edad: Int? = null,
    val objetivo: String? = null
)
