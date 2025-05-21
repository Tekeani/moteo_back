package moteo_back

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // Installation du plugin CORS pour permettre les requêtes depuis l'app mobile
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        anyHost() // À restreindre en production
    }

    // Gestion des erreurs internes
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText("500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    // Déclaration des routes
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

        // Inclusion des routes utilisateur (assure-toi que la fonction userRoutes() existe bien)
        userRoutes()
    }
}
