package org.example.client.core.network

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkConfigTest {
    @Test
    fun exposesAndroidAndLocalHosts() {
        assertEquals("http://10.0.2.2:8080", BaseUrl.AndroidEmulator)
        assertEquals("http://127.0.0.1:8080", BaseUrl.AndroidDevice)
        assertEquals("http://127.0.0.1:8080", BaseUrl.LocalDesktop)
    }

    @Test
    fun acceptsExplicitBaseUrl() {
        assertEquals("http://localhost:9090", NetworkConfig(baseUrl = "http://localhost:9090").baseUrl)
    }
}
