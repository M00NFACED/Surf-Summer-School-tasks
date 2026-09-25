package org.example.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.example.client.core.network.NetworkConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(config = NetworkConfig(baseUrl = BuildConfig.API_BASE_URL))
        }
    }
}
