package org.example.client.core.network

data class NetworkConfig(
    val baseUrl: String = BaseUrl.AndroidEmulator,
    val enableLogging: Boolean = true,
)

object BaseUrl {
    const val AndroidEmulator = "http://10.0.2.2:8080"
    const val LocalDesktop = "http://127.0.0.1:8080"
}
