package moteo_back

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.SQLException

import kotlinx.serialization.Serializable

fun Route.userRoutes() {
    route("/users") {
        post("/register") {
            try {
                // Récupérer les données utilisateur de la requête
                val user = call.receive<User>()

                // Valider les données
                if (user.pseudo.isBlank() || user.password.isBlank() || user.city.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UserResponse(success = false, message = "Tous les champs sont obligatoires")
                    )
                    return@post
                }

                // Vérifier si l'utilisateur existe déjà
                Database.connect().use { connection ->
                    // Vérifier si le pseudo existe déjà
                    val checkStatement = connection.prepareStatement("SELECT pseudo FROM users WHERE pseudo = ?")
                    checkStatement.setString(1, user.pseudo)
                    val resultSet = checkStatement.executeQuery()

                    if (resultSet.next()) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            UserResponse(success = false, message = "Ce pseudo est déjà utilisé")
                        )
                        return@post
                    }

                    // Insérer le nouvel utilisateur
                    val insertStatement = connection.prepareStatement(
                        "INSERT INTO users (pseudo, password, city) VALUES (?, ?, ?)"
                    )
                    insertStatement.setString(1, user.pseudo)
                    insertStatement.setString(2, user.password)  // Note: idéalement, il faudrait hasher le mot de passe
                    insertStatement.setString(3, user.city)

                    val rowsAffected = insertStatement.executeUpdate()
                    if (rowsAffected > 0) {
                        call.respond(
                            HttpStatusCode.Created,
                            UserResponse(success = true, message = "Utilisateur créé avec succès")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            UserResponse(success = false, message = "Échec de création de l'utilisateur")
                        )
                    }
                }
            } catch (e: SQLException) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    UserResponse(success = false, message = "Erreur de base de données: ${e.message}")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    UserResponse(success = false, message = "Erreur: ${e.message}")
                )
            }
        }

        // Route pour la connexion (à implémenter plus tard)
        post("/login") {
            try {
                val user = call.receive<User>()

                if (user.pseudo.isBlank() || user.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UserResponse(success = false, message = "Pseudo et mot de passe requis")
                    )
                    return@post
                }

                Database.connect().use { connection ->
                    val statement = connection.prepareStatement(
                        "SELECT * FROM users WHERE pseudo = ? AND password = ?"
                    )
                    statement.setString(1, user.pseudo)
                    statement.setString(2, user.password)  // Note: avec un hash, ce serait différent

                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        call.respond(
                            HttpStatusCode.OK,
                            UserResponse(success = true, message = "Connexion réussie")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            UserResponse(success = false, message = "Pseudo ou mot de passe incorrect")
                        )
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    UserResponse(success = false, message = "Erreur: ${e.message}")
                )
            }
        }
    }
}