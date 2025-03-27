package com.example.camerax.navigation

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.camerax.TicketApiService
import com.example.camerax.TicketDataStore
import com.example.camerax.TicketResponse
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

enum class Screen {
    Dashboard, History, Categories, Tickets
}


class AppNavigationController(
    private val viewModel: SharedViewModel,
    private val apiService: TicketApiService,
    private val ticketDataStore: TicketDataStore,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val activity: ComponentActivity
) {
    // MLKit Scanner
    val options = GmsDocumentScannerOptions.Builder()
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .setPageLimit(1)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .build()

    val scanner = GmsDocumentScanning.getClient(options)


    fun processImage(
        imageUri: Uri,
        sourceFileName: String,
        onProcessingStateChange: (Boolean) -> Unit,
        onTicketProcessed: (TicketResponse) -> Unit
    ) {
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

                onProcessingStateChange(true)

                lifecycleScope.launch {
                    try {
                        Log.d("AppNavigation", "Enviando imagen al servidor...")
                        val response = apiService.uploadImage(imagePart)
                        onTicketProcessed(response)
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
                        onProcessingStateChange(false)
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


    fun launchScanner(scannerLauncher: (IntentSenderRequest) -> Unit) {
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    activity,
                    "Error al iniciar el escáner: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}