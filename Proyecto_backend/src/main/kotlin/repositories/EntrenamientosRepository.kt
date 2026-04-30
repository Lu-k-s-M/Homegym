package com.homegym.repositories

import com.homegym.database.EntrenamientosTable
import com.homegym.models.RegistroEntrenamiento
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object EntrenamientosRepository {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun getByUsuarioId(usuarioId: Int): List<RegistroEntrenamiento> = transaction {
        EntrenamientosTable.select { EntrenamientosTable.usuarioId eq usuarioId }
            .orderBy(EntrenamientosTable.fecha to SortOrder.DESC)
            .map(::toRegistro)
    }

    fun create(registro: RegistroEntrenamiento): RegistroEntrenamiento = transaction {
        val id = EntrenamientosTable.insertAndGetId {
            it[usuarioId] = registro.usuarioId
            it[rutinaId] = registro.rutinaId
            it[fecha] = LocalDateTime.parse(registro.fecha, formatter)
            it[duracionMinutos] = registro.duracionMinutos
            it[notas] = registro.notas
        }.value

        registro.copy(id = id)
    }

    private fun toRegistro(row: ResultRow): RegistroEntrenamiento = RegistroEntrenamiento(
        id = row[EntrenamientosTable.id].value,
        usuarioId = row[EntrenamientosTable.usuarioId].value,
        rutinaId = row[EntrenamientosTable.rutinaId].value,
        fecha = row[EntrenamientosTable.fecha].format(formatter),
        duracionMinutos = row[EntrenamientosTable.duracionMinutos],
        notas = row[EntrenamientosTable.notas]
    )
}
