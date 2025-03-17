package com.example.camerax.navigation

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.camerax.R
import com.example.camerax.TicketApiService
import com.example.camerax.TicketDataStore
import com.example.camerax.screens.*
import com.example.camerax.viewmodels.SharedViewModel
import androidx.activity.ComponentActivity
import androidx.compose.foundation.shape.CircleShape
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

enum class Screen {
    Dashboard, History, Categories, Tickets
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    viewModel: SharedViewModel,
    apiService: TicketApiService,
    ticketDataStore: TicketDataStore,
    lifecycleScope: LifecycleCoroutineScope,
    activity: ComponentActivity
) {
    var isLoading by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

    // Configurar MLKit Scanner
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .build()
    }

    val scanner = remember {
        GmsDocumentScanning.getClient(options)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            // Manejar el resultado del escaneo aquí
        }
    }

    // Simula una pantalla de carga de 2 segundos
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            if (!isLoading) {  // Ocultar barra de navegación mientras carga
                NavigationBar {
                    // First two navigation items
                    NavigationBarItem(
                        selected = currentScreen == Screen.Dashboard,
                        onClick = { currentScreen = Screen.Dashboard },
                        label = { Text(text = "Inicio") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.home2),
                                contentDescription = "Inicio",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.History,
                        onClick = { currentScreen = Screen.History },
                        label = { Text(text = "Historial") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.history),
                                contentDescription = "Historial",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    )

                    // Center scan button - MODIFIED HERE
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            scanner.getStartScanIntent(activity)
                                .addOnSuccessListener { intentSender ->
                                    scannerLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build()
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        activity,
                                        "Error al iniciar el escáner: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .offset(y = (-10).dp) // Lift button slightly above the navigation bar
                                    .size(56.dp)
                                    .background(Color.White, shape = CircleShape)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.camera),
                                    contentDescription = "Escanear",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        // Empty label to maintain vertical spacing
                        label = { Box(modifier = Modifier.height(16.dp)) {} }
                    )

                    // Last two navigation items
                    NavigationBarItem(
                        selected = currentScreen == Screen.Categories,
                        onClick = { currentScreen = Screen.Categories },
                        label = { Text(text = "Grupo") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.categorias),
                                contentDescription = "Grupo",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Tickets,
                        onClick = { currentScreen = Screen.Tickets },
                        label = { Text(text = "Tickets") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.bills),
                                contentDescription = "Tickets",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Black
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                SplashScreen {
                    currentScreen = Screen.Dashboard
                }
            } else {
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(viewModel, apiService, ticketDataStore, lifecycleScope, activity)
                    Screen.History -> HistoryScreen(viewModel)
                    Screen.Categories -> CategoriesScreen(viewModel)
                    Screen.Tickets -> TicketsScreen(viewModel)
                }
            }
        }
    }
}
