package com.homegym.routes

import com.homegym.errors.ApiException
import com.homegym.models.RegistroEntrenamiento
import com.homegym.models.Rutina
import com.homegym.repositories.EntrenamientosRepository
import com.homegym.repositories.RutinasRepository
import com.homegym.repositories.UsuariosRepository
import com.homegym.security.currentUsername
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.rutinasYEntrenamientosRoutes() {
    route("/rutinas") {
        get {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            call.respond(RutinasRepository.getByUsuarioId(usuario.id!!))
        }

        post {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val rutinaReq = call.receive<Rutina>()
            val rutinaToSave = rutinaReq.copy(usuarioId = usuario.id!!)
            
            val creada = RutinasRepository.create(rutinaToSave)
            call.respond(HttpStatusCode.Created, creada)
        }

        delete("/{id}") {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val id = call.parameters["id"]?.toIntOrNull() ?: throw ApiException(HttpStatusCode.BadRequest, listOf("Id invalido"))
            
            val eliminado = RutinasRepository.delete(id, usuario.id!!)
            if (eliminado) call.respond(HttpStatusCode.NoContent)
            else throw ApiException(HttpStatusCode.NotFound, listOf("Rutina no encontrada o no pertenece al usuario"))
        }

        post("/multiples_delete") {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val ids = call.receive<List<Int>>()
            RutinasRepository.deleteMultiple(ids, usuario.id!!)
            call.respond(HttpStatusCode.NoContent)
        }

        delete("/limpiar") {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            RutinasRepository.deleteAllByUsuarioId(usuario.id!!)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    route("/entrenamientos") {
        get {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            call.respond(EntrenamientosRepository.getByUsuarioId(usuario.id!!))
        }

        post {
            val username = call.currentUsername() ?: throw ApiException(HttpStatusCode.Unauthorized, listOf("No autenticado"))
            val usuario = UsuariosRepository.getByUsername(username) ?: throw ApiException(HttpStatusCode.NotFound, listOf("Usuario no encontrado"))
            
            val registroReq = call.receive<RegistroEntrenamiento>()
            val registroToSave = registroReq.copy(usuarioId = usuario.id!!)
            
            val creado = EntrenamientosRepository.create(registroToSave)
            call.respond(HttpStatusCode.Created, creado)
        }
    }
}
