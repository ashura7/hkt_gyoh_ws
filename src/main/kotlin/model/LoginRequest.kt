package model

@kotlinx.serialization.Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
