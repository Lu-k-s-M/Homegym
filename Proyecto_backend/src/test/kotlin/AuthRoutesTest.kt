package com.homegym

import com.homegym.database.EjerciciosTable
import com.homegym.database.UsuariosTable
import com.homegym.repositories.UsuariosRepository
import com.homegym.services.AuthService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    @Test
    fun register_requires_admin_token() = testApplication {
        application {
            module()
            transaction {
                EjerciciosTable.deleteAll()
                UsuariosTable.deleteAll()
            }
            AuthService.crearUsuarioInicialSiNoExiste()
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"usuario_test_sin_token","password":"ClaveSegura1!","rol":"user"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun register_rejects_creator_role_in_creation() = testApplication {
        application {
            module()
            transaction {
                EjerciciosTable.deleteAll()
                UsuariosTable.deleteAll()
            }
            AuthService.crearUsuarioInicialSiNoExiste()
        }

        val token = loginAsAdmin()
        val username = uniqueUsername("role")

        try {
            val response = client.post("/api/register") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"username":"$username","password":"ClaveSegura1!","rol":"creator"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        } finally {
            UsuariosRepository.deleteByUsername(username)
        }
    }

    @Test
    fun admin_can_register_user_and_promote_to_creator() = testApplication {
        application {
            module()
            transaction {
                EjerciciosTable.deleteAll()
                UsuariosTable.deleteAll()
            }
            AuthService.crearUsuarioInicialSiNoExiste()
        }

        val token = loginAsAdmin()
        val username = uniqueUsername("promo")

        try {
            val registerResponse = client.post("/api/register") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"username":"$username","password":"ClaveSegura1!","rol":"user"}""")
            }
            assertEquals(HttpStatusCode.Created, registerResponse.status)

            val promoteResponse = client.put("/api/users/$username/role") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"rol":"creator"}""")
            }
            assertEquals(HttpStatusCode.OK, promoteResponse.status)

            val userToken = login(username, "ClaveSegura1!")
            val verifyResponse = client.get("/api/verificar-token") {
                header(HttpHeaders.Authorization, "Bearer $userToken")
            }

            assertEquals(HttpStatusCode.OK, verifyResponse.status)
            assertTrue(verifyResponse.bodyAsText().contains("\"role\":\"CREATOR\""))
        } finally {
            UsuariosRepository.deleteByUsername(username)
        }
    }

    private suspend fun ApplicationTestBuilder.loginAsAdmin(): String =
        login("admin", "admin123")

    private suspend fun ApplicationTestBuilder.login(username: String, password: String): String {
        val response = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$username","password":"$password"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()
        val token = Regex("\"token\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
        assertTrue(!token.isNullOrBlank(), "No token found in login response")
        return token
    }

    private fun uniqueUsername(prefix: String): String =
        "${prefix}_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
}
