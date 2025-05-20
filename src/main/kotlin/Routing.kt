package moteo_back

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    // Installation du plugin Content Negotiation pour le JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    // Installation du plugin CORS pour permettre les requêtes depuis l'app mobile
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost() // Pour le développement, à restreindre en production
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText("500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        get("/") {
            call.respondText("Hello from Moteo backend!")
        }

        get("/db-test") {
            try {
                Database.connect().use { connection ->
                    val result = connection.createStatement().executeQuery("SELECT version();")
                    result.next()
                    val version = result.getString(1)
                    call.respondText("Connected to PostgreSQL: $version")
                }
            } catch (e: Exception) {
                call.respondText("Database connection failed: ${e.message}", status = HttpStatusCode.InternalServerError)
            }
        }

        // Ajout des routes utilisateur
        userRoutes()
    }
}


