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
