package com.example.homegym.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Rutina(
    val id: Int? = null,
    val usuarioId: Int,
    val nombre: String,
    val descripcion: String? = null,
    val ejercicios: List<EjercicioRutina> = emptyList()
)

@Serializable
data class EjercicioRutina(
    val ejercicioId: Int,
    val series: Int,
    val repeticiones: Int,
    val descansoSegundos: Int? = null
)
