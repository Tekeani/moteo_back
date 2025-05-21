package moteo_back

import kotlinx.serialization.Serializable

// Modèles

// Utilisé pour l'inscription
@Serializable
data class User(
    val pseudo: String,
    val password: String,
    val city: String
)

// Utilisé pour la connection
@Serializable
data class LoginRequest(
    val pseudo: String,
    val password: String
)

// Réponse standard de l'API pour User
@Serializable
data class UserResponse(
    val success: Boolean,
    val message: String,
    val pseudo: String? = null
)
