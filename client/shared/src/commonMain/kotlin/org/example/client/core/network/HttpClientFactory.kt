package org.example.client.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(config: NetworkConfig = NetworkConfig()): HttpClient = HttpClient {
    expectSuccess = false
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    if (config.enableLogging) {
        install(Logging) {
            logger = ConsoleLogger
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
            filter { request -> !request.url.toString().contains("/auth/") }
        }
    }
    defaultRequest {
        url(config.baseUrl)
    }
}

private object ConsoleLogger : Logger {
    override fun log(message: String) {
        println(message)
    }
}
