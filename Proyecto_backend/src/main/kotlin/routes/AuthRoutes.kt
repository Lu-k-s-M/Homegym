package com.homegym.routes

import com.homegym.errors.ApiException
import com.homegym.models.ChangeRoleRequest
import com.homegym.models.LoginRequest
import com.homegym.models.RegisterRequest
import com.homegym.models.RolUsuario
import com.homegym.models.TokenResponse
import com.homegym.security.generateToken
import com.homegym.security.jwtConfig
import com.homegym.security.requireAnyRole
import com.homegym.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    post("/login") {
        val loginRequest = call.receive<LoginRequest>()

        val resultado = AuthService.validarCredenciales(
            username = loginRequest.username,
            password = loginRequest.password
        )

        if (!resultado.exitoso && resultado.errores.any { it != "Credenciales invalidas" }) {
            throw ApiException(HttpStatusCode.BadRequest, resultado.errores)
        }

        if (!resultado.exitoso) {
            throw ApiException(HttpStatusCode.Unauthorized, resultado.errores)
        }

        val usuario = resultado.usuario
            ?: throw ApiException(HttpStatusCode.InternalServerError, listOf("Error interno del servidor"))

        val token = generateToken(call.application.jwtConfig(), usuario.username, usuario.rol)
        call.respond(HttpStatusCode.OK, TokenResponse(token = token))
    }

    post("/register") {
        val registerRequest = call.receive<RegisterRequest>()

        println("[DEBUG] Recibida peticion de registro: username=${registerRequest.username}, email=${registerRequest.email}")

        val resultado = AuthService.registrarUsuario(
            username = registerRequest.username,
            password = registerRequest.password,
            email = registerRequest.email,
            rol = "user" // Registro público siempre es rol user
        )

        if (!resultado.exitoso) {
            println("[DEBUG] Error en registro: ${resultado.errores}")
            throw ApiException(
                statusCode = if (resultado.usernameDuplicado) {
                    HttpStatusCode.Conflict
                } else {
                    HttpStatusCode.BadRequest
                },
                errores = resultado.errores
            )
        }

        call.respond(HttpStatusCode.Created, mapOf("mensaje" to "Usuario registrado correctamente"))
    }

    authenticate("auth-jwt") {
        get("/users") {
            call.requireAnyRole(RolUsuario.ADMIN)

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10
            val username = call.request.queryParameters["username"]

            if (page < 1) {
                throw ApiException(HttpStatusCode.BadRequest, listOf("El parametro page debe ser mayor o igual que 1"))
            }

            if (size < 1 || size > 100) {
                throw ApiException(HttpStatusCode.BadRequest, listOf("El parametro size debe estar entre 1 y 100"))
            }

            call.respond(HttpStatusCode.OK, AuthService.listarUsuarios(page, size, username))
        }

        put("/users/{username}/role") {
            call.requireAnyRole(RolUsuario.ADMIN)

            val username = call.parameters["username"]
                ?: throw ApiException(HttpStatusCode.BadRequest, listOf("El username es obligatorio"))

            val request = call.receive<ChangeRoleRequest>()
            val resultado = AuthService.cambiarRol(username, request.rol)

            if (!resultado.exitoso) {
                throw ApiException(
                    statusCode = if (resultado.noEncontrado) HttpStatusCode.NotFound else HttpStatusCode.BadRequest,
                    errores = resultado.errores
                )
            }

            call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Rol actualizado correctamente"))
        }

        get("/verificar-token") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal?.payload?.getClaim("username")?.asString()
            val rol = principal?.payload?.getClaim("role")?.asString()
            call.respond(HttpStatusCode.OK, mapOf("mensaje" to "Token valido", "username" to username, "role" to rol))
        }
    }
}
