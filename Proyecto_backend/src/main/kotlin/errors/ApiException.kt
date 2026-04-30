package com.homegym.errors

import io.ktor.http.HttpStatusCode

class ApiException(
    val statusCode: HttpStatusCode,
    val errores: List<String>
) : RuntimeException(errores.joinToString("; "))
