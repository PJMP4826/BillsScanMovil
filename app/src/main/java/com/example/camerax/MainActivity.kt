package com.example.camerax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.camerax.config.AppConfig
import com.example.camerax.data.TicketDataStore
import com.example.camerax.navigation.AppNavigation
import com.example.camerax.repositories.TicketRepository
import com.example.camerax.ui.theme.CameraXTheme
import com.example.camerax.viewmodels.SharedViewModel

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar repositorio y ViewModel
        val repository = TicketRepository(this)
        viewModel = SharedViewModel(repository)

        // Inicializar servicios y componentes
        val scanner = AppConfig.createDocumentScanner(this)
        val apiService = AppConfig.createApiService()
        val ticketDataStore = TicketDataStore(this)

        setContent {
            CameraXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        apiService = apiService,
                        ticketDataStore = ticketDataStore,
                        lifecycleScope = lifecycleScope,
                        activity = this
                    )
                }
            }
        }
    }
}