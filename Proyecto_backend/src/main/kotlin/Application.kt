package com.homegym

import com.homegym.database.DatabaseFactory
import com.homegym.errors.ApiException
import com.homegym.routes.authRoutes
import com.homegym.routes.ejerciciosRoutes
import com.homegym.routes.historialRoutes
import com.homegym.routes.perfilRoutes
import com.homegym.routes.rutinasYEntrenamientosRoutes
import com.homegym.security.configureSecurity
import com.homegym.services.AuthService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init(environment.config)
    AuthService.crearUsuarioInicialSiNoExiste()

    if (pluginOrNull(Authentication) == null) {
        configureSecurity()
    }

    if (pluginOrNull(ContentNegotiation) == null) {
        install(ContentNegotiation) {
            json()
        }
    }

    if (pluginOrNull(StatusPages) == null) {
        install(StatusPages) {
            exception<ApiException> { call, cause ->
                call.respond(cause.statusCode, mapOf("errores" to cause.errores))
            }
            exception<BadRequestException> { call, _ ->
                call.respond(HttpStatusCode.BadRequest, mapOf("errores" to listOf("Cuerpo de la peticion invalido")))
            }
            exception<ContentTransformationException> { call, _ ->
                call.respond(HttpStatusCode.BadRequest, mapOf("errores" to listOf("Cuerpo de la peticion invalido")))
            }
            exception<Throwable> { call, _ ->
                call.respond(HttpStatusCode.InternalServerError, mapOf("errores" to listOf("Error interno del servidor")))
            }
            status(HttpStatusCode.NotFound) { call, _ ->
                call.respond(HttpStatusCode.NotFound, mapOf("errores" to listOf("Recurso no encontrado")))
            }
        }
    }

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi.yaml") {
            version = "4.15.5"
        }
        openAPI(path = "openapi", swaggerFile = "openapi.yaml")

        route("/api") {
            authRoutes()
            authenticate("auth-jwt") {
                ejerciciosRoutes()
                perfilRoutes()
                rutinasYEntrenamientosRoutes()
                historialRoutes()
            }
        }
    }
}
