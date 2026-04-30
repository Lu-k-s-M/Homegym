package com.homegym.security

import com.homegym.errors.ApiException
import com.homegym.models.RolUsuario
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.currentRole(): RolUsuario? =
    RolUsuario.fromValue(principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString())

fun ApplicationCall.requireAnyRole(vararg roles: RolUsuario) {
    val currentRole = currentRole()
    if (currentRole == null || currentRole !in roles.toSet()) {
        throw ApiException(HttpStatusCode.Forbidden, listOf("No tienes permisos para realizar esta accion"))
    }
}

fun ApplicationCall.currentUsername(): String? =
    principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
