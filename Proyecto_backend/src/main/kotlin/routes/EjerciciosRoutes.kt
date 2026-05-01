package com.homegym.routes

import com.homegym.errors.ApiException
import com.homegym.models.Ejercicio
import com.homegym.models.RolUsuario
import com.homegym.security.requireAnyRole
import com.homegym.services.EjerciciosService
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.auth.*

fun Route.ejerciciosRoutes() {
    route("/ejercicios") {
        get {
            // Permitir acceso público a la lista de ejercicios
            call.respond(EjerciciosService.getEjercicios())
        }

        get("/{id}") {
            // Permitir acceso público al detalle de un ejercicio
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                throw ApiException(HttpStatusCode.BadRequest, listOf("Id invalido"))
            }

            val ejercicio = EjerciciosService.getEjercicio(id)

            if (ejercicio != null) {
                call.respond(ejercicio)
            } else {
                throw ApiException(HttpStatusCode.NotFound, listOf("Ejercicio no encontrado"))
            }
        }

        authenticate("auth-jwt") {
            post {
                call.requireAnyRole(RolUsuario.CREATOR, RolUsuario.ADMIN)
                val ejercicio = call.receive<Ejercicio>()

                val errores = EjerciciosService.validarEjercicio(ejercicio, permitirIdEnBody = false)
                if (errores.isNotEmpty()) {
                    throw ApiException(HttpStatusCode.BadRequest, errores)
                }

                val creado = EjerciciosService.createEjercicio(ejercicio)
                call.respond(HttpStatusCode.Created, creado)
            }

            put("/{id}") {
                call.requireAnyRole(RolUsuario.CREATOR, RolUsuario.ADMIN)
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    throw ApiException(HttpStatusCode.BadRequest, listOf("Id invalido"))
                }

                val ejercicio = call.receive<Ejercicio>()

                val errores = EjerciciosService.validarEjercicio(
                    ejercicio = ejercicio,
                    idEsperado = id,
                    permitirIdEnBody = true
                )
                if (errores.isNotEmpty()) {
                    throw ApiException(HttpStatusCode.BadRequest, errores)
                }

                val actualizado = EjerciciosService.updateEjercicio(id, ejercicio)

                if (actualizado) call.respond(HttpStatusCode.OK)
                else throw ApiException(HttpStatusCode.NotFound, listOf("Ejercicio no encontrado"))
            }

            delete("/{id}") {
                call.requireAnyRole(RolUsuario.ADMIN)
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    throw ApiException(HttpStatusCode.BadRequest, listOf("Id invalido"))
                }

                val eliminado = EjerciciosService.deleteEjercicio(id)

                if (eliminado) call.respond(HttpStatusCode.NoContent)
                else throw ApiException(HttpStatusCode.NotFound, listOf("Ejercicio no encontrado"))
            }
        }
    }
}
