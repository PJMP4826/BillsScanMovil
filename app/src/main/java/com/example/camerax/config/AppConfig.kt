package com.example.camerax.config

import android.content.Context
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit
import com.example.camerax.models.TicketResponse


// Interfaz de API
interface TicketApiService {
    @Multipart
    @POST("/procesar_ticket")
    suspend fun uploadImage(@Part image: MultipartBody.Part): TicketResponse
}

object AppConfig {
    // Configuración del escáner de documentos
    fun createDocumentScanner(context: Context) = GmsDocumentScanning.getClient(
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .setPageLimit(3)
            .setGalleryImportAllowed(false)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .build()
    )

    // Configuración de Retrofit
    fun createApiService(): TicketApiService {
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

        return retrofit.create(TicketApiService::class.java)
    }
}