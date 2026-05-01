package com.homegym.models

import kotlinx.serialization.Serializable

@Serializable
data class Rutina(
    val id: Int? = null,
    val usuarioId: Int,
    val nombre: String,
    val descripcion: String? = null,
    val duracionMinutos: Int = 0,
    val calorias: Int = 0,
    val ejercicios: List<EjercicioRutina> = emptyList()
)

@Serializable
data class EjercicioRutina(
    val ejercicioId: Int,
    val series: Int,
    val repeticiones: Int,
    val descansoSegundos: Int? = 0
)

@Serializable
data class RegistroEntrenamiento(
    val id: Int? = null,
    val usuarioId: Int,
    val rutinaId: Int,
    val fecha: String, // ISO-8601
    val duracionMinutos: Int? = null,
    val notas: String? = null
)
