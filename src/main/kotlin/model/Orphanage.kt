package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Orphanage(
    @SerialName("orphanage_name") val name: String,
    @SerialName("owner_name") val owner: String,
    val email: String,
    val password: String,
    val address: String
)