package org.example.client.features.auth.domain

data class ClientSession(
    val accessToken: String,
    val client: Client? = null,
)
