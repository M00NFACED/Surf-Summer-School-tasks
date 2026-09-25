package org.example.client.features.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeRequest(
    @SerialName("phone") val phone: String,
    @SerialName("code") val code: String,
)
