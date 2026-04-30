package com.homegym.models

enum class RolUsuario {
    USER,
    CREATOR,
    ADMIN;

    companion object {
        fun fromValue(value: String?): RolUsuario? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
