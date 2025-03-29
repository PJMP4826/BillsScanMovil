package com.example.camerax.navigation

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.camerax.R
import com.example.camerax.config.TicketApiService
import com.example.camerax.data.TicketDataStore
import com.example.camerax.models.TicketResponse
import com.example.camerax.screens.*
import com.example.camerax.viewmodels.SharedViewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

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
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    var ticketResponse by remember { mutableStateOf<TicketResponse?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val navigationController = remember {
        AppNavigationController(viewModel, apiService, ticketDataStore, lifecycleScope, activity)
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pages?.firstOrNull()?.let { page ->
                scannedImageUri = page.imageUri

                val timestamp = System.currentTimeMillis()
                val fileName = "scan_$timestamp.jpg"

                scannedImageUri?.let { uri ->
                    navigationController.processImage(
                        imageUri = uri,
                        sourceFileName = fileName,
                        onProcessingStateChange = { processing -> isProcessing = processing },
                        onTicketProcessed = { response -> ticketResponse = response }
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        isLoading = false
    }


    if (isProcessing) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar */ },
            title = { Text("Procesando...") },
            text = { Text("Por favor espera mientras se procesa la imagen.") },
            confirmButton = {}
        )
    }

    Scaffold(
        bottomBar = {
            if (!isLoading) {
                NavigationBar(
                    containerColor = Color(0xFF2A4D8A)
                ) {
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
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFDAAA3F),
                            selectedIconColor = Color(0xFFDAAA3F),
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black
                        )
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
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFDAAA3F),
                            selectedIconColor = Color(0xFFDAAA3F),
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black
                        )
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navigationController.launchScanner { request ->
                                scannerLauncher.launch(request)
                            }
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .offset(y = (-10).dp)
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
                        label = { Box(modifier = Modifier.height(16.dp)) {} }
                    )

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
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFDAAA3F),
                            selectedIconColor = Color(0xFFDAAA3F),
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black
                        )
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
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFDAAA3F),
                            selectedIconColor = Color(0xFFDAAA3F),
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black
                        )
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
                    Screen.Dashboard -> DashboardScreen(
                        viewModel,
                        apiService,
                        ticketDataStore,
                        lifecycleScope,
                        activity
                    )

                    Screen.History -> HistoryScreen(viewModel)
                    Screen.Categories -> CategoriesScreen(viewModel)
                    Screen.Tickets -> TicketsScreen(viewModel)
                }
            }
        }
    }
}