package org.example.client.features.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    @SerialName("id") val id: String,
    @SerialName("phone") val phone: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
