package com.homegym.database

import io.ktor.server.config.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    private var initialized = false

    fun init(config: ApplicationConfig) {
        if (initialized) return
        val databaseConfig = config.config("ktor.database")
        val url = databaseConfig.property("url").getString()
        val user = databaseConfig.property("user").getString()
        val password = databaseConfig.property("password").getString()
        val driver = databaseConfig.property("driver").getString()

        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                EjerciciosTable,
                UsuariosTable,
                PerfilesTable,
                RutinasTable,
                RutinaEjerciciosTable,
                EntrenamientosTable,
                HistorialTable
            )
            migrarTablaEjerciciosAntigua()
            crearEjerciciosDePrueba()
        }
        initialized = true
    }

    private fun Transaction.crearEjerciciosDePrueba() {
        val count = EjerciciosTable.selectAll().count()
        if (count == 0L) {
            val ejercicios = listOf(
                com.homegym.models.Ejercicio(null, "Flexiones de pecho", "Ejercicio de fuerza para pecho y brazos", "Intermedia", "Pecho y Tríceps", 100, "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                com.homegym.models.Ejercicio(null, "Sentadillas", "Ejercicio fundamental de pierna", "Básica", "Piernas", 150, "https://images.unsplash.com/photo-1574680096145-d05b474e2158?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                com.homegym.models.Ejercicio(null, "Plancha abdominal", "Ejercicio isométrico para el abdomen", "Media", "Core", 80, "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                com.homegym.models.Ejercicio(null, "Dominadas", "Ejercicio de tracción superior", "Avanzada", "Espalda y Bíceps", 200, "https://images.unsplash.com/photo-1581009146145-b5ef03a74715?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4")
            )
            ejercicios.forEach { ej ->
                EjerciciosTable.insert {
                    it[nombre] = ej.nombre
                    it[descripcion] = ej.descripcion
                    it[intensidad] = ej.intensidad
                    it[parteCuerpo] = ej.parteCuerpo
                    it[calorias] = ej.calorias
                    it[imagenUrl] = ej.imagenUrl
                    it[videoUrl] = ej.videoUrl
                }
            }
        }
    }

    private fun Transaction.migrarTablaEjerciciosAntigua() {
        val tablaAntiguaExiste = exec(
            """
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'tablaejercicio'
            LIMIT 1
            """.trimIndent()
        ) { resultSet ->
            resultSet.next()
        } ?: false

        if (!tablaAntiguaExiste) return

        exec(
            """
            INSERT INTO ejercicios (nombre, descripcion, intensidad, parte_del_cuerpo, calorias)
            SELECT t.nombre, t.descripcion, t.intensidad, t.parte_del_cuerpo, t.calorias
            FROM tablaejercicio t
            WHERE NOT EXISTS (
                SELECT 1
                FROM ejercicios e
                WHERE e.nombre = t.nombre
                  AND e.descripcion = t.descripcion
                  AND e.intensidad = t.intensidad
                  AND e.parte_del_cuerpo = t.parte_del_cuerpo
                  AND e.calorias = t.calorias
            )
            """.trimIndent()
        )
    }
}
