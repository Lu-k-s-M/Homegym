package com.homegym.routes

import com.homegym.errors.ApiException
import com.homegym.models.PerfilUsuario
import com.homegym.repositories.PerfilesRepository
import com.homegym.repositories.UsuariosRepository
import com.homegym.security.currentUsername
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.perfilRoutes() {
    route("/perfil") {
        get {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val perfil = PerfilesRepository.getByUsuarioId(usuario.id!!)
                ?: PerfilUsuario(usuarioId = usuario.id)
            
            call.respond(perfil)
        }

        put {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val perfilReq = call.receive<PerfilUsuario>()
            val perfilToSave = perfilReq.copy(usuarioId = usuario.id!!)
            
            val guardado = PerfilesRepository.upsert(perfilToSave)
            call.respond(HttpStatusCode.OK, guardado)
        }
    }
}
