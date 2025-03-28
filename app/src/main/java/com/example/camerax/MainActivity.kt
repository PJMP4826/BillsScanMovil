package com.example.camerax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.camerax.ui.theme.CameraXTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import androidx.lifecycle.lifecycleScope
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit
import com.example.camerax.data.TicketDataStore
import com.example.camerax.navigation.AppNavigation
import com.example.camerax.repositories.TicketRepository
import com.example.camerax.viewmodels.SharedViewModel


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
    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = TicketRepository(this)
        viewModel = SharedViewModel(repository)

        val options = GmsDocumentScannerOptions.Builder()
            .setScannerMode(SCANNER_MODE_FULL)
            .setPageLimit(3)
            .setGalleryImportAllowed(false)
            .setResultFormats(RESULT_FORMAT_JPEG)
            .build()

        val scanner = GmsDocumentScanning.getClient(options)
        val ticketDataStore = TicketDataStore(this)

        // Configuracion de Retrofit con Timeout extendido
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://3.147.172.159:8080") // endpoint apiTickets en AWS
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(TicketApiService::class.java)

        setContent {
            CameraXTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
