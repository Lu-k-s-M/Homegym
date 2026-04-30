package com.homegym.models

import kotlinx.serialization.Serializable

@Serializable
data class HistorialEjercicio(
    val id: Int? = null,
    val usuarioId: Int,
    val ejercicioId: Int,
    val fecha: String, // Usaremos String para facilitar la serialización
    val ejercicioNombre: String? = null,
    val ejercicioImagen: String? = null
)
