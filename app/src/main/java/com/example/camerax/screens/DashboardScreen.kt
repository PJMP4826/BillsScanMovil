package com.example.camerax.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import coil.compose.AsyncImage
import com.example.camerax.R
import com.example.camerax.config.TicketApiService
import com.example.camerax.models.TicketResponse
import com.example.camerax.data.TicketDataStore
import com.example.camerax.models.DetalleCompra
import com.example.camerax.models.Ticket
import com.example.camerax.viewmodels.SharedViewModel
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImagePreviewDialog(
    ticket: Ticket,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Image display
                AsyncImage(
                    model = Uri.parse(ticket.imageUri),
                    contentDescription = "Ticket Image Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Preview",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTicketThumbnail(
    ticket: Ticket,
    onImageClick: (Ticket) -> Unit
) {
    Card(
        modifier = Modifier
            .width(85.dp)
            .height(230.dp)
            .clickable { onImageClick(ticket) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        AsyncImage(
            model = Uri.parse(ticket.imageUri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun DashboardScreen(
    viewModel: SharedViewModel,
    apiService: TicketApiService,
    ticketDataStore: TicketDataStore,
    lifecycleScope: LifecycleCoroutineScope,
    activity: ComponentActivity
) {
    val context = LocalContext.current
    val recentTickets by viewModel.recentTickets.collectAsState(initial = emptyList())
    var scannedImageUri by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var ticketResponse by remember { mutableStateOf<TicketResponse?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedTicketForPreview by remember { mutableStateOf<Ticket?>(null) }

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

    // Modificación en el método processImage dentro de DashboardScreen.kt
    fun processImage(imageUri: Uri, sourceFileName: String) {
        Log.d("DashboardScreen", "Iniciando procesamiento de imagen: $sourceFileName")

        // Guardar una copia del archivo en el almacenamiento interno
        val destinationFile = File(context.filesDir, sourceFileName)

        try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Log.d("DashboardScreen", "Imagen copiada al almacenamiento interno: ${destinationFile.absolutePath}")

            if (destinationFile.exists()) {
                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), destinationFile)
                val imagePart = MultipartBody.Part.createFormData("imagen", destinationFile.name, requestFile)

                isProcessing = true

                lifecycleScope.launch {
                    try {
                        Log.d("DashboardScreen", "Enviando imagen al servidor...")
                        val response = apiService.uploadImage(imagePart)
                        ticketResponse = response
                        Log.d("DashboardScreen", "Respuesta recibida del servidor")

                        response?.let {
                            Log.d("DashboardScreen", "Guardando ticket en TicketDataStore")
                            // Guardar en el TicketDataStore
                            ticketDataStore.saveTicket(it, imageUri)

                            // Crear objeto Ticket
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

                            // Añadir al ViewModel
                            viewModel.addNewTicket(newTicket)

                            // Refrescar explícitamente los tickets
                            viewModel.refreshTickets()

                            Log.d("DashboardScreen", "Proceso completado. Ticket añadido y UI actualizada")
                        }
                    } catch (e: Exception) {
                        Log.e("UploadError", "Error al procesar la imagen: ${e.message}", e)
                        Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isProcessing = false
                    }
                }
            } else {
                Log.e("FileError", "El archivo de destino no existe")
                Toast.makeText(context, "No se pudo guardar la imagen localmente", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("FileError", "Error al guardar la imagen: ${e.message}", e)
            Toast.makeText(context, "Error al guardar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            scannedImageUri = listOf(selectedUri)

            // Generar un nombre de archivo único basado en timestamp
            val timestamp = System.currentTimeMillis()
            val fileName = "gallery_$timestamp.jpg"

            // Procesar la imagen usando la función común
            processImage(selectedUri, fileName)
        }
    }

    // Launcher para el escáner de documentos
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)

            scanningResult?.pages?.firstOrNull()?.let { page ->
                scannedImageUri = listOf(page.imageUri)

                // Generar un nombre de archivo único basado en timestamp
                val timestamp = System.currentTimeMillis()
                val fileName = "scan_$timestamp.jpg"

                // Procesar la imagen usando la función común
                processImage(page.imageUri, fileName)
            }
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD3D3D3)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.33f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.cloud),
                        contentDescription = "Subir",
                        modifier = Modifier.size(100.dp),
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                    ) {
                        // Escanear Button
                        Button(
                            onClick = {
                                scanner.getStartScanIntent(activity)
                                    .addOnSuccessListener { intentSender ->
                                        scannerLauncher.launch(
                                            IntentSenderRequest.Builder(intentSender).build()
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error al iniciar el escáner: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDAAA3F)),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                "Escanear",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Archivos Recientes Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.67f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Archivos Recientes header
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        color = Color(0xFF1F3E73),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Archivos Recientes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (recentTickets.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(recentTickets) { ticket ->
                                RecentTicketThumbnail(
                                    ticket = ticket,
                                    onImageClick = { selectedTicket ->
                                        selectedTicketForPreview = selectedTicket
                                    }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay archivos recientes",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        selectedTicketForPreview?.let { ticket ->
            ImagePreviewDialog(
                ticket = ticket,
                onDismiss = {
                    selectedTicketForPreview = null
                }
            )
        }
    }
}