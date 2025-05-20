package moteo_back

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val pseudo: String,
    val password: String,
    val city: String
)

@Serializable
data class UserResponse(
    val success: Boolean,
    val message: String
)