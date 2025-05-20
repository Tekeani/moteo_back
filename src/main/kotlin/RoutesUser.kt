package moteo_back

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import java.sql.SQLException

@Serializable
data class User(val pseudo: String, val password: String, val city: String = "")

@Serializable
data class UserResponse(val success: Boolean, val message: String)

fun Route.userRoutes() {
    route("/users") {

        // 🔐 Inscription avec hash du mot de passe
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
                    checkStatement.setString(1, user.pseudo)
                    val resultSet = checkStatement.executeQuery()

                    if (resultSet.next()) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            UserResponse(success = false, message = "Ce pseudo est déjà utilisé")
                        )
                        return@post
                    }

                    val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

                    val insertStatement = connection.prepareStatement(
                        "INSERT INTO users (pseudo, password, city) VALUES (?, ?, ?)"
                    )
                    insertStatement.setString(1, user.pseudo)
                    insertStatement.setString(2, hashedPassword)
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

        // 🔐 Connexion sécurisée avec comparaison de mot de passe hashé
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
                        "SELECT password FROM users WHERE pseudo = ?"
                    )
                    statement.setString(1, user.pseudo)
                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        val storedHashedPassword = resultSet.getString("password")

                        if (BCrypt.checkpw(user.password, storedHashedPassword)) {
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

                    resultSet.close()
                    statement.close()
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
