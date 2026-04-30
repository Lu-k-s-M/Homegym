package com.homegym.repositories

import com.homegym.database.EjerciciosTable
import com.homegym.models.Ejercicio
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object EjerciciosRepository {

    fun getAll(): List<Ejercicio> = transaction {
        EjerciciosTable
            .selectAll()
            .orderBy(EjerciciosTable.id to SortOrder.ASC)
            .map(::toEjercicio)
    }

    fun getById(id: Int): Ejercicio? = transaction {
        EjerciciosTable
            .select { EjerciciosTable.id eq id }
            .map(::toEjercicio)
            .singleOrNull()
    }

    fun create(ejercicio: Ejercicio): Ejercicio = transaction {
        val id = EjerciciosTable.insertAndGetId {
            it[nombre] = ejercicio.nombre
            it[descripcion] = ejercicio.descripcion
            it[intensidad] = ejercicio.intensidad
            it[parteCuerpo] = ejercicio.parteCuerpo
            it[calorias] = ejercicio.calorias
            it[imagenUrl] = ejercicio.imagenUrl
            it[videoUrl] = ejercicio.videoUrl
        }.value

        ejercicio.copy(id = id)
    }

    fun update(id: Int, ejercicio: Ejercicio): Boolean = transaction {
        EjerciciosTable.update({ EjerciciosTable.id eq id }) {
            it[nombre] = ejercicio.nombre
            it[descripcion] = ejercicio.descripcion
            it[intensidad] = ejercicio.intensidad
            it[parteCuerpo] = ejercicio.parteCuerpo
            it[calorias] = ejercicio.calorias
            it[imagenUrl] = ejercicio.imagenUrl
            it[videoUrl] = ejercicio.videoUrl
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        EjerciciosTable.deleteWhere { EjerciciosTable.id eq id } > 0
    }

    private fun toEjercicio(row: ResultRow): Ejercicio =
        Ejercicio(
            id = row[EjerciciosTable.id].value,
            nombre = row[EjerciciosTable.nombre],
            descripcion = row[EjerciciosTable.descripcion],
            intensidad = row[EjerciciosTable.intensidad],
            parteCuerpo = row[EjerciciosTable.parteCuerpo],
            calorias = row[EjerciciosTable.calorias],
            imagenUrl = row[EjerciciosTable.imagenUrl],
            videoUrl = row[EjerciciosTable.videoUrl]
        )
}
