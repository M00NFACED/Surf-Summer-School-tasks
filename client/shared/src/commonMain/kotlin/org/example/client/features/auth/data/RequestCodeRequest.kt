package org.example.client.features.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestCodeRequest(
    @SerialName("phone") val phone: String,
)
