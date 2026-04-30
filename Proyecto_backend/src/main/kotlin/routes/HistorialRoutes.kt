package com.homegym.routes

import com.homegym.errors.ApiException
import com.homegym.models.RolUsuario
import com.homegym.repositories.HistorialRepository
import com.homegym.repositories.UsuariosRepository
import com.homegym.security.currentUsername
import com.homegym.security.requireAnyRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.historialRoutes() {
    val historialRepository = HistorialRepository()

    route("/historial") {
        get {
            call.requireAnyRole(RolUsuario.USER, RolUsuario.CREATOR, RolUsuario.ADMIN)
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val historial = historialRepository.getHistorialPorUsuario(usuario.id!!)
            call.respond(historial)
        }

        post("/ejercicio/{ejercicioId}") {
            call.requireAnyRole(RolUsuario.USER, RolUsuario.CREATOR, RolUsuario.ADMIN)
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val ejercicioId = call.parameters["ejercicioId"]?.toIntOrNull()
                ?: throw ApiException(HttpStatusCode.BadRequest, listOf("ID de ejercicio inválido"))
            
            historialRepository.agregarAlHistorial(usuario.id!!, ejercicioId)
            call.respond(HttpStatusCode.Created)
        }

        delete("/{id}") {
            call.requireAnyRole(RolUsuario.USER, RolUsuario.CREATOR, RolUsuario.ADMIN)
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException(HttpStatusCode.BadRequest, listOf("ID de historial inválido"))
            
            historialRepository.eliminarDelHistorial(id)
            call.respond(HttpStatusCode.NoContent)
        }

        delete("/multiples") {
            call.requireAnyRole(RolUsuario.USER, RolUsuario.CREATOR, RolUsuario.ADMIN)
            val ids = call.receive<List<Int>>()
            historialRepository.eliminarMultiples(ids)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/multiples_delete") {
            call.requireAnyRole(RolUsuario.USER, RolUsuario.CREATOR, RolUsuario.ADMIN)
            val ids = call.receive<List<Int>>()
            historialRepository.eliminarMultiples(ids)
            call.respond(HttpStatusCode.NoContent)
        }

        delete("/limpiar") {
            call.requireAnyRole(RolUsuario.USER, RolUsuario.CREATOR, RolUsuario.ADMIN)
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            historialRepository.limpiarHistorial(usuario.id!!)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
