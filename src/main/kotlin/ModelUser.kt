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

// Utilisé pour la connexion (pas besoin de "city")
@Serializable
data class LoginRequest(
    val pseudo: String,
    val password: String
)

// Réponse standard de l'API pour User, avec pseudo optionnel
@Serializable
data class UserResponse(
    val success: Boolean,
    val message: String,
    val pseudo: String? = null  // Champ optionnel ajouté
)
