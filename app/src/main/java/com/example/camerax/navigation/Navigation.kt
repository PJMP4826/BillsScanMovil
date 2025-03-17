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
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.example.camerax.models.Ticket
import com.example.camerax.models.DetalleCompra
import com.example.camerax.TicketResponse

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

    // Nueva variable para almacenar la URI de la imagen escaneada
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    var ticketResponse by remember { mutableStateOf<TicketResponse?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Función para procesar la imagen
    fun processImage(imageUri: Uri, sourceFileName: String) {
        Log.d("AppNavigation", "Iniciando procesamiento de imagen: $sourceFileName")
        val destinationFile = File(activity.filesDir, sourceFileName)

        try {
            activity.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d(
                "AppNavigation",
                "Imagen copiada al almacenamiento interno: ${destinationFile.absolutePath}"
            )

            if (destinationFile.exists()) {
                val requestFile =
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), destinationFile)
                val imagePart =
                    MultipartBody.Part.createFormData("imagen", destinationFile.name, requestFile)

                isProcessing = true

                lifecycleScope.launch {
                    try {
                        Log.d("AppNavigation", "Enviando imagen al servidor...")
                        val response = apiService.uploadImage(imagePart)
                        ticketResponse = response
                        Log.d("AppNavigation", "Respuesta recibida del servidor")

                        response?.let {
                            Log.d("AppNavigation", "Guardando ticket en TicketDataStore")
                            ticketDataStore.saveTicket(it, imageUri)

                            val newTicket = Ticket(
                                empresa = it.resultado.encabezado.nombre_empresa,
                                fecha = it.resultado.encabezado.fecha,
                                hora = it.resultado.encabezado.hora,
                                imageUri = imageUri.toString(),
                                detalles = it.resultado.detalle_compra.map { detalle ->
                                    DetalleCompra(
                                        cantidad = detalle.cantidad,
                                        descripcion = detalle.descripcion,
                                        precioUnitario = detalle.precio_unitario,
                                        subtotal = detalle.subtotal
                                    )
                                }
                            )

                            viewModel.addNewTicket(newTicket)
                            viewModel.refreshTickets()
                            Log.d(
                                "AppNavigation",
                                "Proceso completado. Ticket añadido y UI actualizada"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("UploadError", "Error al procesar la imagen: ${e.message}", e)
                        Toast.makeText(
                            activity,
                            "Error al procesar la imagen: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isProcessing = false
                    }
                }
            } else {
                Log.e("FileError", "El archivo de destino no existe")
                Toast.makeText(
                    activity,
                    "No se pudo guardar la imagen localmente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e("FileError", "Error al guardar la imagen: ${e.message}", e)
            Toast.makeText(activity, "Error al guardar la imagen: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scanningResult?.pages?.firstOrNull()?.let { page ->
                scannedImageUri = page.imageUri

                // Generar un nombre de archivo único basado en timestamp
                val timestamp = System.currentTimeMillis()
                val fileName = "scan_$timestamp.jpg"

                // Procesar la imagen usando la función común
                scannedImageUri?.let { uri -> processImage(uri, fileName) }
            }
        }
    }

    // Simula una pantalla de carga de 2 segundos
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        isLoading = false
    }

    // Mostrar un diálogo de procesamiento mientras isProcessing es true
    if (isProcessing) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar */ },
            title = { Text("Procesando...") },
            text = { Text("Por favor espera mientras se procesa la imagen.") },
            confirmButton = { /* Sin botón de confirmación */ }
        )
    }

    Scaffold(
        bottomBar = {
            if (!isLoading) {  // Ocultar barra de navegación mientras carga
                NavigationBar(
                    containerColor = Color(0xFF2A4D8A)
                ) {
                    // First navigation item
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

                    // Second navigation item
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

                    // Center scan button (No se cambia)
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

                    // Third navigation item
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

                    // Fourth navigation item
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
