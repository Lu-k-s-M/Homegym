package com.homegym.repositories

import com.homegym.database.RutinaEjerciciosTable
import com.homegym.database.RutinasTable
import com.homegym.models.EjercicioRutina
import com.homegym.models.Rutina
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object RutinasRepository {

    fun getByUsuarioId(usuarioId: Int): List<Rutina> = transaction {
        RutinasTable.select { RutinasTable.usuarioId eq usuarioId }
            .map { row ->
                val id = row[RutinasTable.id].value
                Rutina(
                    id = id,
                    usuarioId = row[RutinasTable.usuarioId].value,
                    nombre = row[RutinasTable.nombre],
                    descripcion = row[RutinasTable.descripcion],
                    ejercicios = getEjerciciosDeRutina(id)
                )
            }
    }

    fun getById(id: Int): Rutina? = transaction {
        RutinasTable.select { RutinasTable.id eq id }
            .map { row ->
                Rutina(
                    id = id,
                    usuarioId = row[RutinasTable.usuarioId].value,
                    nombre = row[RutinasTable.nombre],
                    descripcion = row[RutinasTable.descripcion],
                    ejercicios = getEjerciciosDeRutina(id)
                )
            }.singleOrNull()
    }

    private fun getEjerciciosDeRutina(rutinaId: Int): List<EjercicioRutina> {
        return RutinaEjerciciosTable.select { RutinaEjerciciosTable.rutinaId eq rutinaId }
            .map {
                EjercicioRutina(
                    ejercicioId = it[RutinaEjerciciosTable.ejercicioId].value,
                    series = it[RutinaEjerciciosTable.series],
                    repeticiones = it[RutinaEjerciciosTable.repeticiones],
                    descansoSegundos = it[RutinaEjerciciosTable.descansoSegundos]
                )
            }
    }

    fun create(rutina: Rutina): Rutina = transaction {
        val id = RutinasTable.insertAndGetId {
            it[usuarioId] = rutina.usuarioId
            it[nombre] = rutina.nombre
            it[descripcion] = rutina.descripcion
        }.value

        rutina.ejercicios.forEach { ej ->
            RutinaEjerciciosTable.insert {
                it[rutinaId] = id
                it[ejercicioId] = ej.ejercicioId
                it[series] = ej.series
                it[repeticiones] = ej.repeticiones
                it[descansoSegundos] = ej.descansoSegundos
            }
        }

        rutina.copy(id = id)
    }

    fun delete(id: Int, usuarioId: Int): Boolean = transaction {
        RutinasTable.deleteWhere { (RutinasTable.id eq id) and (RutinasTable.usuarioId eq usuarioId) } > 0
    }
}
