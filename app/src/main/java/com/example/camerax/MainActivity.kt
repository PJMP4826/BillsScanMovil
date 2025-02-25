package com.example.camerax

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.camerax.ui.theme.CameraXTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.camerax.screens.ScanScreen
import com.example.camerax.screens.HistoryScreen

interface TicketApiService {
    @Multipart
    @POST("/procesar_ticket")
    suspend fun uploadImage(@Part image: MultipartBody.Part): TicketResponse
}

data class TicketResponse(val resultado: TicketResult)

data class TicketResult(val detalle_compra: List<CompraDetalle>, val encabezado: Encabezado)

data class CompraDetalle(val cantidad: Int, val descripcion: String, val precio_unitario: Double, val subtotal: Double)

data class Encabezado(val nombre_empresa: String, val fecha: String, val hora: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setPageLimit(3)
            .setGalleryImportAllowed(false)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        val ticketDataStore = TicketDataStore(this)

        // Configuración de Retrofit con Timeout extendido
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)  // Tiempo de espera para la conexión
            .writeTimeout(30, TimeUnit.SECONDS)    // Tiempo de espera para la escritura
            .readTimeout(30, TimeUnit.SECONDS)     // Tiempo de espera para leer la respuesta
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.219.14:5000") // Asegúrate de que esta sea la URL correcta
            .client(client)  // Usar el cliente configurado con tiempos de espera
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(TicketApiService::class.java)

        setContent {
            CameraXTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var selectedTab by remember { mutableStateOf(0) }
                    var scannedImageUri by remember { mutableStateOf<List<Uri>>(emptyList()) }
                    var ticketResponse by remember { mutableStateOf<TicketResponse?>(null) }

                    val scannerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) {
                        if (it.resultCode == RESULT_OK) {
                            val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(it.data)
                            scanningResult?.pages?.let { pages ->
                                scannedImageUri = listOf(pages[0].imageUri)
                                val imageFile = File(filesDir, "scan.jpg")
                                contentResolver.openInputStream(pages[0].imageUri)?.use { inputStream ->
                                    FileOutputStream(imageFile).use { outputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }

                                // Verifica si el archivo se creó correctamente
                                if (imageFile.exists()) {
                                    val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageFile)
                                    val imagePart = MultipartBody.Part.createFormData("imagen", imageFile.name, requestFile)

                                    Log.d("Upload", "Iniciando subida de imagen...")

                                    lifecycleScope.launch {
                                        try {
                                            // Subir la imagen
                                            ticketResponse = apiService.uploadImage(imagePart)
                                            ticketResponse?.let { response ->
                                                // Save both ticket and image URI
                                                ticketDataStore.saveTicket(response, pages[0].imageUri)
                                            }
                                            Log.d("Upload", "Respuesta recibida: $ticketResponse")
                                        } catch (e: Exception) {
                                            // Mostrar mensaje de error más claro
                                            Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                                            Toast.makeText(applicationContext, "Error al subir la imagen: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    Log.e("UploadError", "El archivo de imagen no se creó correctamente.")
                                    Toast.makeText(applicationContext, "Error: El archivo de imagen no se creó correctamente.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    Column {
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Scan") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("History") }
                            )
                        }

                        when (selectedTab) {
                            0 -> ScanScreen(
                                scannedImageUri = scannedImageUri,
                                ticketResponse = ticketResponse,
                                onScanClick = {
                                    scanner.getStartScanIntent(this@MainActivity)
                                        .addOnSuccessListener { intentSender ->
                                            scannerLauncher.launch(
                                                IntentSenderRequest.Builder(intentSender).build()
                                            )
                                        }
                                        .addOnFailureListener {
                                            Log.e("ScanError", "Error al iniciar el escaneo: ${it.message}")
                                            Toast.makeText(applicationContext, "Error al iniciar el escaneo: ${it.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                            )
                            1 -> HistoryScreen()
                        }
                    }
                }
            }
        }
    }
}
