package com.homegym.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.homegym.models.RolUsuario
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.Date

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)

fun Application.jwtConfig(): JwtConfig {
    val config = environment.config.config("ktor.jwt")
    return JwtConfig(
        secret = config.property("secret").getString(),
        issuer = config.property("issuer").getString(),
        audience = config.property("audience").getString(),
        realm = config.property("realm").getString()
    )
}

fun Application.configureSecurity() {
    val jwtConfig = jwtConfig()
    val algorithm = Algorithm.HMAC256(jwtConfig.secret)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(algorithm)
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )
            validate { credential ->
                val username = credential.payload.getClaim("username").asString()
                val rol = credential.payload.getClaim("role").asString()
                if (username.isNullOrBlank() || RolUsuario.fromValue(rol) == null) {
                    null
                } else {
                    JWTPrincipal(credential.payload)
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("errores" to listOf("Token invalido o ausente")))
            }
        }
    }
}

fun generateToken(jwtConfig: JwtConfig, username: String, rol: RolUsuario): String {
    val algorithm = Algorithm.HMAC256(jwtConfig.secret)
    return JWT.create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("username", username)
        .withClaim("role", rol.name)
        .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
        .sign(algorithm)
}
