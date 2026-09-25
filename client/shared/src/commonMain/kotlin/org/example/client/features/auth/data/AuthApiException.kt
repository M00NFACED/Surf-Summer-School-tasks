package org.example.client.features.auth.data

class AuthApiException(
    val statusCode: Int,
    val errorCode: String,
    override val message: String,
) : Exception(message)
