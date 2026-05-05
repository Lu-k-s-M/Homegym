package com.homegym.repositories

import com.homegym.database.PerfilesTable
import com.homegym.models.PerfilUsuario
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PerfilesRepository {

    fun getByUsuarioId(usuarioId: Int): PerfilUsuario? = transaction {
        PerfilesTable.select { PerfilesTable.usuarioId eq usuarioId }
            .map {
                PerfilUsuario(
                    id = it[PerfilesTable.id].value,
                    usuarioId = it[PerfilesTable.usuarioId].value,
                    peso = it[PerfilesTable.peso],
                    altura = it[PerfilesTable.altura],
                    edad = it[PerfilesTable.edad],
                    objetivo = it[PerfilesTable.objetivo]
                )
            }.singleOrNull()
    }

    fun upsert(perfil: PerfilUsuario): PerfilUsuario = transaction {
        val existing = getByUsuarioId(perfil.usuarioId)
        if (existing == null) {
            val id = PerfilesTable.insertAndGetId {
                it[usuarioId] = perfil.usuarioId
                it[peso] = perfil.peso
                it[altura] = perfil.altura
                it[edad] = perfil.edad
                it[objetivo] = perfil.objetivo
            }.value
            perfil.copy(id = id)
        } else {
            PerfilesTable.update({ PerfilesTable.usuarioId eq perfil.usuarioId }) {
                it[peso] = perfil.peso
                it[altura] = perfil.altura
                it[edad] = perfil.edad
                it[objetivo] = perfil.objetivo
            }
            perfil.copy(id = existing.id)
        }
    }

    fun deleteByUsuarioId(usuarioId: Int): Boolean = transaction {
        PerfilesTable.deleteWhere { PerfilesTable.usuarioId eq usuarioId } > 0
    }
}
