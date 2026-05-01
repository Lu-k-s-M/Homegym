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
            // Forzamos la limpieza de ejercicios antiguos para asegurar que se carguen los nuevos
            // Solo si queremos asegurar que el volumen de datos es el correcto
            val count = EjerciciosTable.selectAll().count()
            if (count < 19L) {
                // Si hay pocos, es mejor limpiar y poner todos los nuevos
                RutinaEjerciciosTable.deleteAll()
                HistorialTable.deleteAll()
                EjerciciosTable.deleteAll()
            }
            
            crearEjerciciosDePrueba()
            migrarTablaEjerciciosAntigua()
        }
        initialized = true
    }

    private fun Transaction.crearEjerciciosDePrueba() {
        val count = EjerciciosTable.selectAll().count()
        // Si hay pocos ejercicios, añadimos más para dar volumen
        if (count < 19L) {
            val ejercicios = listOf(
                // Existentes (por si se limpia la tabla)
                com.homegym.models.Ejercicio(null, "Flexiones de pecho", "Ejercicio de fuerza para pecho y brazos", "Intermedia", "Pecho y Tríceps", 100, "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                com.homegym.models.Ejercicio(null, "Sentadillas", "Ejercicio fundamental de pierna", "Básica", "Piernas", 150, "https://images.unsplash.com/photo-1574680096145-d05b474e2158?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                com.homegym.models.Ejercicio(null, "Plancha abdominal", "Ejercicio isométrico para el abdomen", "Media", "Core", 80, "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                com.homegym.models.Ejercicio(null, "Dominadas", "Ejercicio de tracción superior", "Avanzada", "Espalda y Bíceps", 200, "https://images.unsplash.com/photo-1581009146145-b5ef03a74715?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),

                // CARDIO / AERÓBICOS
                com.homegym.models.Ejercicio(null, "Burpees", "Ejercicio cardiovascular de alta intensidad", "Alta", "Full Body", 300, "https://images.unsplash.com/photo-1599058917232-d750c8217072?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                com.homegym.models.Ejercicio(null, "Jumping Jacks", "Saltos de estrella para activar el corazón", "Baja", "Cardio", 50, "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                com.homegym.models.Ejercicio(null, "Saltar la cuerda", "Excelente ejercicio aeróbico", "Media", "Cardio", 250, "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                com.homegym.models.Ejercicio(null, "Mountain Climbers", "Simulación de escalada en suelo", "Media", "Core y Cardio", 120, "https://images.unsplash.com/photo-1434596922112-19c563067271?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),

                // PIERNA / GLÚTEO
                com.homegym.models.Ejercicio(null, "Zancadas (Lunges)", "Fortalecimiento de piernas y glúteos", "Media", "Piernas", 140, "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                com.homegym.models.Ejercicio(null, "Puente de glúteo", "Aislamiento de glúteos", "Baja", "Glúteos", 60, "https://images.unsplash.com/photo-1518310383802-640c2de311b2?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                com.homegym.models.Ejercicio(null, "Peso muerto rumano", "Enfoque en isquiotibiales y glúteos", "Avanzada", "Pierna posterior", 180, "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                com.homegym.models.Ejercicio(null, "Sentadilla Búlgara", "Intenso trabajo unilateral de pierna", "Alta", "Piernas", 160, "https://images.unsplash.com/photo-1574680096145-d05b474e2158?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),

                // ESPALDA / BRAZOS
                com.homegym.models.Ejercicio(null, "Remo con mancuerna", "Fortalecimiento dorsal", "Media", "Espalda", 130, "https://images.unsplash.com/photo-1581009146145-b5ef03a74715?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                com.homegym.models.Ejercicio(null, "Supermans", "Extensión lumbar para espalda baja", "Baja", "Espalda", 40, "https://images.unsplash.com/photo-1598971861713-54ad16a7e72e?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                com.homegym.models.Ejercicio(null, "Curl de bíceps", "Aislamiento de bíceps", "Baja", "Bíceps", 70, "https://images.unsplash.com/photo-1581009146145-b5ef03a74715?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
                com.homegym.models.Ejercicio(null, "Dips de tríceps", "Trabajo de tríceps en banco o silla", "Media", "Tríceps", 90, "https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"),

                // CORE
                com.homegym.models.Ejercicio(null, "Dead Bug", "Estabilidad del core profundo", "Baja", "Core", 50, "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                com.homegym.models.Ejercicio(null, "Ruso (Twist)", "Rotación abdominal", "Media", "Core", 80, "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                com.homegym.models.Ejercicio(null, "Elevación de piernas", "Trabajo de abdomen inferior", "Media", "Core", 100, "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=400", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
            )
            ejercicios.forEach { ej ->
                // Verificamos si ya existe por nombre para evitar duplicados si la tabla no está vacía
                val existe = EjerciciosTable.select { EjerciciosTable.nombre eq ej.nombre }.any()
                if (!existe) {
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
