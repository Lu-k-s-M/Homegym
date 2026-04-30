package com.homegym.database

import org.jetbrains.exposed.dao.id.IntIdTable

object UsuariosTable : IntIdTable("usuarios") {
    val username = varchar("username", 100).uniqueIndex()
    val password = varchar("password", 255)
    val email = varchar("email", 255).nullable()
    val rol = varchar("rol", 20).default("USER")
}
