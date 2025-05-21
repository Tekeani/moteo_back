package moteo_back

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import java.sql.SQLException

// Modèles importés
import moteo_back.User
import moteo_back.LoginRequest
import moteo_back.UserResponse

fun Route.userRoutes() {
    route("/users") {

        // Test bcrypt simple
        get("/test-bcrypt") {
            val password = "monMotDePasse123"
            val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
            val isPasswordValid = BCrypt.checkpw(password, hashedPassword)

            call.respondText(
                "Mot de passe: $password\nHash: $hashedPassword\nCheck password OK? $isPasswordValid"
            )
        }

        // Inscription avec hash du mot de passe
        post("/register") {
            try {
                val user = call.receive<User>()

                if (user.pseudo.isBlank() || user.password.isBlank() || user.city.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UserResponse(success = false, message = "Tous les champs sont obligatoires")
                    )
                    return@post
                }

                Database.connect().use { connection ->
                    val checkStatement = connection.prepareStatement("SELECT pseudo FROM users WHERE pseudo = ?")
                    checkStatement.use { stmt ->
                        stmt.setString(1, user.pseudo)
                        val resultSet = stmt.executeQuery()
                        if (resultSet.next()) {
                            call.respond(
                                HttpStatusCode.Conflict,
                                UserResponse(success = false, message = "Ce pseudo est déjà utilisé")
                            )
                            return@post
                        }
                    }

                    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

                    val insertStatement = connection.prepareStatement(
                        "INSERT INTO users (pseudo, password, city) VALUES (?, ?, ?)"
                    )
                    insertStatement.use { stmt ->
                        stmt.setString(1, user.pseudo)
                        stmt.setString(2, hashedPassword)
                        stmt.setString(3, user.city)

                        val rowsAffected = stmt.executeUpdate()
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

        // Connection
        post("/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()

                if (loginRequest.pseudo.isBlank() || loginRequest.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UserResponse(success = false, message = "Pseudo et mot de passe requis")
                    )
                    return@post
                }

                Database.connect().use { connection ->
                    val statement = connection.prepareStatement(
                        "SELECT password FROM users WHERE pseudo = ?"
                    )
                    statement.use { stmt ->
                        stmt.setString(1, loginRequest.pseudo)
                        val resultSet = stmt.executeQuery()
                        if (resultSet.next()) {
                            val storedHashedPassword = resultSet.getString("password")

                            if (BCrypt.checkpw(loginRequest.password, storedHashedPassword)) {
                                call.respond(
                                    HttpStatusCode.OK,
                                    UserResponse(success = true, message = "Connexion réussie")
                                )
                            } else {
                                call.respond(
                                    HttpStatusCode.Unauthorized,
                                    UserResponse(success = false, message = "Mot de passe incorrect")
                                )
                            }
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                UserResponse(success = false, message = "Utilisateur introuvable")
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    UserResponse(success = false, message = "Erreur: ${e.message}")
                )
            }
        }

        // Mise à jour utilisateur (avec hash du nouveau mot de passe)
        put("/update") {
            try {
                val user = call.receive<User>()

                if (user.pseudo.isBlank() || user.password.isBlank() || user.city.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        UserResponse(success = false, message = "Tous les champs sont obligatoires")
                    )
                    return@put
                }

                Database.connect().use { connection ->
                    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

                    val updateStatement = connection.prepareStatement(
                        "UPDATE users SET password = ?, city = ? WHERE pseudo = ?"
                    )
                    updateStatement.use { stmt ->
                        stmt.setString(1, hashedPassword)
                        stmt.setString(2, user.city)
                        stmt.setString(3, user.pseudo)

                        val rowsUpdated = stmt.executeUpdate()
                        if (rowsUpdated > 0) {
                            call.respond(
                                HttpStatusCode.OK,
                                UserResponse(success = true, message = "Utilisateur mis à jour avec succès")
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                UserResponse(success = false, message = "Utilisateur non trouvé")
                            )
                        }
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

        // Récupère l'utilisateur
        get("/profile") {
            val pseudo = call.request.queryParameters["pseudo"]
            if (pseudo.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    UserResponse(success = false, message = "Paramètre pseudo manquant")
                )
                return@get
            }

            try {
                Database.connect().use { connection ->
                    val statement = connection.prepareStatement(
                        "SELECT pseudo, city FROM users WHERE pseudo = ?"
                    )
                    statement.use { stmt ->
                        stmt.setString(1, pseudo)
                        val resultSet = stmt.executeQuery()
                        if (resultSet.next()) {
                            val city = resultSet.getString("city")
                            call.respond(
                                HttpStatusCode.OK,
                                UserProfileResponse(pseudo = pseudo, city = city)
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.NotFound,
                                UserResponse(success = false, message = "Utilisateur non trouvé")
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    UserResponse(success = false, message = "Erreur serveur: ${e.message}")
                )
            }
        }
    }
}

// Nouvelle classe pour la réponse du profil utilisateur
@kotlinx.serialization.Serializable
data class UserProfileResponse(
    val pseudo: String,
    val city: String
)
