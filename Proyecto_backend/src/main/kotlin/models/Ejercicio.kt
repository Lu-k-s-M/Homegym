package com.homegym.models

import kotlinx.serialization.Serializable

@Serializable
data class Ejercicio(
    val id: Int? = null,
    val nombre: String,
    val descripcion: String,
    val intensidad: String,
    val parteCuerpo: String,
    val calorias: Int,
    val imagenUrl: String? = null,
    val videoUrl: String? = null
)
