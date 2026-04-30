package com.homegym.services

import com.homegym.models.RolUsuario
import com.homegym.models.UsuariosPaginadosResponse
import com.homegym.models.UsuarioResponse
import com.homegym.models.Usuario
import com.homegym.repositories.UsuariosRepository
import org.mindrot.jbcrypt.BCrypt

object AuthService {

    fun listarUsuarios(page: Int, size: Int, username: String?): UsuariosPaginadosResponse {
        val usuariosPage = UsuariosRepository.findPage(page, size, username)
        val usuarios = usuariosPage.items.mapNotNull { usuario ->
            val id = usuario.id ?: return@mapNotNull null
            UsuarioResponse(
                id = id,
                username = usuario.username,
                rol = usuario.rol.name
            )
        }
        val totalItems = usuariosPage.totalItems
        val totalPages = if (totalItems == 0) 0 else ((totalItems - 1) / size) + 1

        return UsuariosPaginadosResponse(
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages,
            items = usuarios
        )
    }

    data class RegistroResultado(
        val exitoso: Boolean,
        val errores: List<String> = emptyList(),
        val usernameDuplicado: Boolean = false
    )

    data class CambioRolResultado(
        val exitoso: Boolean,
        val errores: List<String> = emptyList(),
        val noEncontrado: Boolean = false
    )

    data class LoginResultado(
        val exitoso: Boolean,
        val errores: List<String> = emptyList(),
        val usuario: Usuario? = null
    )

    fun validarCredenciales(username: String, password: String): LoginResultado {
        val errores = validarLogin(username, password)
        if (errores.isNotEmpty()) {
            return LoginResultado(exitoso = false, errores = errores)
        }

        val usuario = UsuariosRepository.getByUsername(username)
            ?: return LoginResultado(exitoso = false, errores = listOf("Credenciales invalidas"))

        val passwordValida = when {
            esHashBcrypt(usuario.password) -> BCrypt.checkpw(password, usuario.password)
            usuario.password == password -> {
                val nuevoHash = hashPassword(password)
                if (usuario.id != null) {
                    UsuariosRepository.updatePasswordHash(usuario.id, nuevoHash)
                }
                true
            }
            else -> false
        }

        return if (passwordValida) {
            LoginResultado(exitoso = true, usuario = usuario)
        } else {
            LoginResultado(exitoso = false, errores = listOf("Credenciales invalidas"))
        }
    }

    fun crearUsuarioInicialSiNoExiste() {
        val adminExistente = UsuariosRepository.getByUsername("admin")

        if (adminExistente == null && !UsuariosRepository.existsAnyUser()) {
            UsuariosRepository.create(
                Usuario(
                    username = "admin",
                    password = hashPassword("admin123"),
                    rol = RolUsuario.ADMIN
                )
            )
            return
        }

        if (adminExistente != null && adminExistente.rol != RolUsuario.ADMIN && adminExistente.id != null) {
            UsuariosRepository.updateRol(adminExistente.id, RolUsuario.ADMIN)
        }
    }

    fun registrarUsuario(username: String, password: String, email: String?, rol: String?): RegistroResultado {
        val finalRol = if (rol.isNullOrBlank()) "user" else rol
        val errores = validarRegistro(username, password, finalRol)
        if (errores.isNotEmpty()) {
            return RegistroResultado(exitoso = false, errores = errores)
        }

        if (UsuariosRepository.getByUsername(username) != null) {
            return RegistroResultado(
                exitoso = false,
                errores = listOf("El username ya existe"),
                usernameDuplicado = true
            )
        }

        UsuariosRepository.create(
            Usuario(
                username = username,
                password = hashPassword(password),
                email = email,
                rol = RolUsuario.fromValue(rol) ?: RolUsuario.USER
            )
        )

        return RegistroResultado(exitoso = true)
    }

    fun cambiarRol(username: String, nuevoRol: String): CambioRolResultado {
        val errores = mutableListOf<String>()

        if (username.isBlank()) {
            errores += "El username es obligatorio"
        }

        val rol = RolUsuario.fromValue(nuevoRol)
        if (nuevoRol.isBlank()) {
            errores += "El rol es obligatorio"
        } else if (rol == null || rol !in setOf(RolUsuario.CREATOR, RolUsuario.ADMIN)) {
            errores += "Solo se puede cambiar el rol a creator o admin"
        }

        if (errores.isNotEmpty()) {
            return CambioRolResultado(exitoso = false, errores = errores)
        }

        val usuario = UsuariosRepository.getByUsername(username)
            ?: return CambioRolResultado(
                exitoso = false,
                errores = listOf("Usuario no encontrado"),
                noEncontrado = true
            )

        if (usuario.rol == rol) {
            return CambioRolResultado(
                exitoso = false,
                errores = listOf("El usuario ya tiene ese rol")
            )
        }

        val actualizado = UsuariosRepository.updateRolByUsername(username, rol!!)
        return if (actualizado) {
            CambioRolResultado(exitoso = true)
        } else {
            CambioRolResultado(
                exitoso = false,
                errores = listOf("No se pudo actualizar el rol")
            )
        }
    }

    private fun validarRegistro(username: String, password: String, rol: String): List<String> {
        val errores = mutableListOf<String>()

        println("[DEBUG] Validando registro - username: '$username', password length: ${password.length}, rol: '$rol'")

        if (username.isBlank()) {
            errores += "El username es obligatorio"
        } else if (username.length < 3) {
            errores += "El username debe tener al menos 3 caracteres"
        } else if (username.length > 100) {
            errores += "El username debe tener como maximo 100 caracteres"
        } else if (!username.matches(Regex("^[A-Za-z0-9_.-]+$"))) {
            errores += "El username solo puede contener letras, numeros, puntos, guiones y guiones bajos"
        }

        if (password.isBlank()) {
            errores += "La password es obligatoria"
        } else if (password.length < 10) {
            errores += "La password debe tener al menos 10 caracteres"
        } else if (password.length > 72) {
            errores += "La password debe tener como maximo 72 caracteres"
        } else {
            if (!password.any { it.isUpperCase() }) {
                errores += "La password debe incluir al menos una letra mayuscula"
            }
            if (!password.any { it.isLowerCase() }) {
                errores += "La password debe incluir al menos una letra minuscula"
            }
            if (!password.any { it.isDigit() }) {
                errores += "La password debe incluir al menos un numero"
            }
            if (!password.any { !it.isLetterOrDigit() && !it.isWhitespace() }) {
                errores += "La password debe incluir al menos un simbolo"
            }
            if (password.contains(username, ignoreCase = true)) {
                errores += "La password no puede contener el username"
            }
        }

        val rolNormalizado = RolUsuario.fromValue(rol)
        if (rol.isBlank()) {
            errores += "El rol es obligatorio"
        } else if (rolNormalizado == null) {
            errores += "El rol debe ser user. Recibido: '$rol'"
        } else if (rolNormalizado != RolUsuario.USER) {
            errores += "El registro solo puede crear usuarios con rol user"
        }

        if (errores.isNotEmpty()) {
            println("[DEBUG] Errores de validacion encontrados: $errores")
        }

        return errores
    }

    private fun validarLogin(username: String, password: String): List<String> {
        val errores = mutableListOf<String>()

        if (username.isBlank()) {
            errores += "El username es obligatorio"
        }

        if (password.isBlank()) {
            errores += "La password es obligatoria"
        }

        return errores
    }

    private fun hashPassword(password: String): String =
        BCrypt.hashpw(password, BCrypt.gensalt())

    private fun esHashBcrypt(password: String): Boolean =
        password.startsWith("\$2a$") || password.startsWith("\$2b$") || password.startsWith("\$2y$")
}
