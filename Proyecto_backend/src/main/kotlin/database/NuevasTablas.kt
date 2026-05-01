package com.homegym.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object PerfilesTable : IntIdTable("perfiles") {
    val usuarioId = reference("usuario_id", UsuariosTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val peso = double("peso").nullable()
    val altura = double("altura").nullable()
    val edad = integer("edad").nullable()
    val objetivo = varchar("objetivo", 255).nullable()
}

object RutinasTable : IntIdTable("rutinas") {
    val usuarioId = reference("usuario_id", UsuariosTable, onDelete = ReferenceOption.CASCADE)
    val nombre = varchar("nombre", 100)
    val descripcion = text("descripcion").nullable()
    val duracionMinutos = integer("duracion_minutos").default(0)
    val calorias = integer("calorias").default(0)
}

object RutinaEjerciciosTable : IntIdTable("rutina_ejercicios") {
    val rutinaId = reference("rutina_id", RutinasTable, onDelete = ReferenceOption.CASCADE)
    val ejercicioId = reference("ejercicio_id", EjerciciosTable, onDelete = ReferenceOption.CASCADE)
    val series = integer("series")
    val repeticiones = integer("repeticiones")
    val descansoSegundos = integer("descanso_segundos").nullable()
}

object EntrenamientosTable : IntIdTable("entrenamientos") {
    val usuarioId = reference("usuario_id", UsuariosTable, onDelete = ReferenceOption.CASCADE)
    val rutinaId = reference("rutina_id", RutinasTable, onDelete = ReferenceOption.CASCADE)
    val fecha = datetime("fecha")
    val duracionMinutos = integer("duracion_minutos").nullable()
    val notas = text("notas").nullable()
}

object HistorialTable : IntIdTable("historial") {
    val usuarioId = reference("usuario_id", UsuariosTable, onDelete = ReferenceOption.CASCADE)
    val ejercicioId = reference("ejercicio_id", EjerciciosTable, onDelete = ReferenceOption.CASCADE)
    val fecha = datetime("fecha")
}
