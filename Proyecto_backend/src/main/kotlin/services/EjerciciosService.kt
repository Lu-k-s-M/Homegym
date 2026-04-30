package com.homegym.services

import com.homegym.models.Ejercicio
import com.homegym.repositories.EjerciciosRepository

object EjerciciosService {

    fun getEjercicios() = EjerciciosRepository.getAll()

    fun getEjercicio(id: Int) = EjerciciosRepository.getById(id)

    fun createEjercicio(ejercicio: Ejercicio) =
        EjerciciosRepository.create(ejercicio)

    fun updateEjercicio(id: Int, ejercicio: Ejercicio) =
        EjerciciosRepository.update(id, ejercicio)

    fun deleteEjercicio(id: Int) =
        EjerciciosRepository.delete(id)

    fun validarEjercicio(ejercicio: Ejercicio, idEsperado: Int? = null, permitirIdEnBody: Boolean): List<String> {
        val errores = mutableListOf<String>()

        if (!permitirIdEnBody && ejercicio.id != null) {
            errores += "El id no debe enviarse en el cuerpo de la peticion"
        }

        if (idEsperado != null && ejercicio.id != null && ejercicio.id != idEsperado) {
            errores += "El id del body debe coincidir con el id de la ruta"
        }

        if (ejercicio.nombre.isBlank()) {
            errores += "El nombre es obligatorio"
        } else if (ejercicio.nombre.length > 100) {
            errores += "El nombre debe tener como maximo 100 caracteres"
        }

        if (ejercicio.descripcion.isBlank()) {
            errores += "La descripcion es obligatoria"
        } else if (ejercicio.descripcion.length > 1000) {
            errores += "La descripcion debe tener como maximo 1000 caracteres"
        }

        if (ejercicio.intensidad.isBlank()) {
            errores += "La intensidad es obligatoria"
        } else if (ejercicio.intensidad.length > 50) {
            errores += "La intensidad debe tener como maximo 50 caracteres"
        }

        if (ejercicio.parteCuerpo.isBlank()) {
            errores += "La parte del cuerpo es obligatoria"
        } else if (ejercicio.parteCuerpo.length > 50) {
            errores += "La parte del cuerpo debe tener como maximo 50 caracteres"
        }

        if (ejercicio.calorias <= 0) {
            errores += "Las calorias deben ser mayores que 0"
        }

        return errores
    }
}
