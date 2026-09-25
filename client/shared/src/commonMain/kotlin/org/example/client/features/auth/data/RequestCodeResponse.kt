package org.example.client.features.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestCodeResponse(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("retry_after") val retryAfter: Int? = null,
)
