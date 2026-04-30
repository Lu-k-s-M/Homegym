package com.homegym.repositories

import com.homegym.database.EjerciciosTable
import com.homegym.database.HistorialTable
import com.homegym.models.HistorialEjercicio
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistorialRepository {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun getHistorialPorUsuario(usuarioId: Int): List<HistorialEjercicio> = transaction {
        (HistorialTable innerJoin EjerciciosTable)
            .select { HistorialTable.usuarioId eq usuarioId }
            .orderBy(HistorialTable.fecha, SortOrder.DESC)
            .map {
                HistorialEjercicio(
                    id = it[HistorialTable.id].value,
                    usuarioId = it[HistorialTable.usuarioId].value,
                    ejercicioId = it[HistorialTable.ejercicioId].value,
                    fecha = it[HistorialTable.fecha].format(formatter),
                    ejercicioNombre = it[EjerciciosTable.nombre],
                    ejercicioImagen = it[EjerciciosTable.imagenUrl]
                )
            }
    }

    fun agregarAlHistorial(usuarioId: Int, ejercicioId: Int) = transaction {
        val existente = HistorialTable.select {
            (HistorialTable.usuarioId eq usuarioId) and (HistorialTable.ejercicioId eq ejercicioId)
        }.singleOrNull()

        if (existente != null) {
            HistorialTable.update({ HistorialTable.id eq existente[HistorialTable.id] }) {
                it[this.fecha] = LocalDateTime.now()
            }
        } else {
            HistorialTable.insert {
                it[this.usuarioId] = usuarioId
                it[this.ejercicioId] = ejercicioId
                it[this.fecha] = LocalDateTime.now()
            }
        }
    }

    fun eliminarDelHistorial(id: Int) = transaction {
        HistorialTable.deleteWhere { HistorialTable.id eq id }
    }

    fun eliminarMultiples(ids: List<Int>) = transaction {
        HistorialTable.deleteWhere { HistorialTable.id inList ids }
    }

    fun limpiarHistorial(usuarioId: Int) = transaction {
        HistorialTable.deleteWhere { HistorialTable.usuarioId eq usuarioId }
    }
}
