package com.example.camerax.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.camerax.viewmodels.SharedViewModel
import com.example.camerax.models.Ticket
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.example.camerax.TicketApiService
import com.example.camerax.TicketDataStore
import com.example.camerax.TicketResponse
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import androidx.activity.ComponentActivity
import com.example.camerax.models.DetalleCompra
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

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

    // Configurar MLKit Scanner
    val options = remember {
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setPageLimit(1)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .build()
    }
    
    val scanner = remember {
        GmsDocumentScanning.getClient(options)
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            scannedImageUri = listOf(selectedUri)
            val imageFile = File(context.filesDir, "upload.jpg")
            context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                FileOutputStream(imageFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (imageFile.exists()) {
                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
                val imagePart = MultipartBody.Part.createFormData("imagen", imageFile.name, requestFile)

                lifecycleScope.launch {
                    try {
                        val response = apiService.uploadImage(imagePart)
                        ticketResponse = response
                        response?.let { 
                            ticketDataStore.saveTicket(it, selectedUri)
                            viewModel.addNewTicket(
                                Ticket(
                                    empresa = it.resultado.encabezado.nombre_empresa,
                                    fecha = it.resultado.encabezado.fecha,
                                    hora = it.resultado.encabezado.hora,
                                    imageUri = selectedUri.toString(),
                                    detalles = it.resultado.detalle_compra.map { detalle ->
                                        DetalleCompra(
                                            cantidad = detalle.cantidad,
                                            descripcion = detalle.descripcion,
                                            precioUnitario = detalle.precio_unitario,
                                            subtotal = detalle.subtotal
                                        )
                                    }
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                        Toast.makeText(context, "Error al subir la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
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
                val imageFile = File(context.filesDir, "scan.jpg")
                context.contentResolver.openInputStream(page.imageUri)?.use { inputStream ->
                    FileOutputStream(imageFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                if (imageFile.exists()) {
                    val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
                    val imagePart = MultipartBody.Part.createFormData("imagen", imageFile.name, requestFile)

                    lifecycleScope.launch {
                        try {
                            val response = apiService.uploadImage(imagePart)
                            ticketResponse = response
                            response?.let { 
                                ticketDataStore.saveTicket(it, page.imageUri)
                                // Actualizar el ViewModel con el nuevo ticket
                                viewModel.addNewTicket(
                                    Ticket(
                                        empresa = it.resultado.encabezado.nombre_empresa,
                                        fecha = it.resultado.encabezado.fecha,
                                        hora = it.resultado.encabezado.hora,
                                        imageUri = page.imageUri.toString(),
                                        detalles = it.resultado.detalle_compra.map { detalle ->
                                            DetalleCompra(
                                                cantidad = detalle.cantidad,
                                                descripcion = detalle.descripcion,
                                                precioUnitario = detalle.precio_unitario,
                                                subtotal = detalle.subtotal
                                            )
                                        }
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                            Toast.makeText(context, "Error al subir la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload")
            }

            Button(
                onClick = { 
                    scanner.getStartScanIntent(activity)  // Now using activity instead of context
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
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Scan")
            }
        }

        // Recent Files Section
        Text(
            "Recent Tickets",
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(recentTickets) { ticket ->
                RecentTicketCard(ticket)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentTicketCard(ticket: Ticket) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        ) {
            // Añadir imagen
            AsyncImage(
                model = Uri.parse(ticket.imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = ticket.empresa,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Fecha: ${ticket.fecha}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Total: $${ticket.total}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
