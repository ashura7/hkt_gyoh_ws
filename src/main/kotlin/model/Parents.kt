package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Parents(
    @SerialName("parent_name") val name: String,
    val gender: Int,
    @SerialName("phone_number") val phone: String,
    val email: String,
    val password: String,
    val address: String
)