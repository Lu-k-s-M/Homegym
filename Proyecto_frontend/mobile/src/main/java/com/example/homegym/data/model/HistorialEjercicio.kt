package com.example.homegym.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HistorialEjercicio(
    val id: Int? = null,
    val usuarioId: Int,
    val ejercicioId: Int,
    val fecha: String,
    val ejercicioNombre: String? = null,
    val ejercicioImagen: String? = null
)
