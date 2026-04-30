package com.homegym.models

import kotlinx.serialization.Serializable

@Serializable
data class PerfilUsuario(
    val id: Int? = null,
    val usuarioId: Int,
    val peso: Double? = null,
    val altura: Double? = null,
    val edad: Int? = null,
    val objetivo: String? = null
)
