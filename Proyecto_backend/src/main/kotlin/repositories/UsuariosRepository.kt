package com.homegym.repositories

import com.homegym.database.UsuariosTable
import com.homegym.models.RolUsuario
import com.homegym.models.Usuario
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

object UsuariosRepository {

    data class UsuariosPage(
        val items: List<Usuario>,
        val totalItems: Int
    )

    fun findPage(page: Int, size: Int, username: String?): UsuariosPage = transaction {
        val filtro = username?.trim().orEmpty()
        val whereClause = if (filtro.isBlank()) {
            Op.TRUE
        } else {
            Op.build { UsuariosTable.username like "%$filtro%" }
        }

        val totalItems = UsuariosTable
            .select { whereClause }
            .count()
            .toInt()

        val offset = ((page - 1) * size).toLong()

        val items = UsuariosTable
            .select { whereClause }
            .orderBy(UsuariosTable.username to SortOrder.ASC)
            .limit(size, offset)
            .map(::toUsuario)

        UsuariosPage(
            items = items,
            totalItems = totalItems
        )
    }

    fun getByUsername(username: String): Usuario? = transaction {
        UsuariosTable
            .select { UsuariosTable.username eq username }
            .map(::toUsuario)
            .singleOrNull()
    }

    fun existsAnyUser(): Boolean = transaction {
        !UsuariosTable.selectAll().empty()
    }

    fun create(usuario: Usuario): Usuario = transaction {
        val id = UsuariosTable.insertAndGetId {
            it[username] = usuario.username
            it[password] = usuario.password
            it[email] = usuario.email
            it[rol] = usuario.rol.name
        }.value

        usuario.copy(id = id)
    }

    fun updatePasswordHash(id: Int, passwordHash: String): Boolean = transaction {
        UsuariosTable.update({ UsuariosTable.id eq id }) {
            it[password] = passwordHash
        } > 0
    }

    fun updateRol(id: Int, rol: RolUsuario): Boolean = transaction {
        UsuariosTable.update({ UsuariosTable.id eq id }) {
            it[UsuariosTable.rol] = rol.name
        } > 0
    }

    fun updateRolByUsername(username: String, rol: RolUsuario): Boolean = transaction {
        UsuariosTable.update({ UsuariosTable.username eq username }) {
            it[UsuariosTable.rol] = rol.name
        } > 0
    }

    fun deleteByUsername(username: String): Boolean = transaction {
        UsuariosTable.deleteWhere { UsuariosTable.username eq username } > 0
    }

    private fun toUsuario(row: ResultRow): Usuario =
        Usuario(
            id = row[UsuariosTable.id].value,
            username = row[UsuariosTable.username],
            password = row[UsuariosTable.password],
            email = row[UsuariosTable.email],
            rol = RolUsuario.fromValue(row[UsuariosTable.rol]) ?: RolUsuario.USER
        )
}
