package com.homegym.database

import org.jetbrains.exposed.dao.id.IntIdTable

object EjerciciosTable : IntIdTable("ejercicios") {
    val nombre = varchar("nombre", 255)
    val descripcion = text("descripcion")
    val intensidad = varchar("intensidad", 50)
    val parteCuerpo = varchar("parte_del_cuerpo", 50)
    val calorias = integer("calorias")
    val imagenUrl = varchar("imagen_url", 500).nullable()
    val videoUrl = varchar("video_url", 500).nullable()
}
