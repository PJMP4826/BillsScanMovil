package com.example.camerax

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.camerax.config.AppConfig
import com.example.camerax.config.TicketApiService
import com.example.camerax.data.TicketDataStore
import com.example.camerax.navigation.controllers.NavigationController
import com.example.camerax.repositories.TicketRepository
import com.example.camerax.ui.theme.CameraXTheme
import com.example.camerax.viewmodels.AuthViewModel
import com.example.camerax.viewmodels.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var navHostController: NavHostController
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SharedViewModel
    private lateinit var apiService: TicketApiService
    private lateinit var ticketDataStore: TicketDataStore
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando MainActivity")

        initializeComponents()

        setContent {
            Log.d(TAG, "onCreate: Iniciando composición")
            CameraXTheme {
                navHostController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                NavigationController(
                    navHostController = navHostController,
                    authViewModel = authViewModel,
                    viewModel = viewModel,
                    apiService = apiService,
                    ticketDataStore = ticketDataStore,
                    lifecycleScope = lifecycleScope,
                    activity = this
                )
            }
        }
    }

    private fun initializeComponents() {
        enableEdgeToEdge()
        Log.d(TAG, "initializeComponents: Edge to edge enabled")

        auth = FirebaseAuth.getInstance()
        Log.d(TAG, "initializeComponents: Firebase Auth inicializado")

        val repository = TicketRepository(this)
        viewModel = SharedViewModel(repository)
        Log.d(TAG, "initializeComponents: Repository y ViewModel inicializados")

        apiService = AppConfig.createApiService()
        ticketDataStore = TicketDataStore(this)
        firestore = FirebaseFirestore.getInstance()
        Log.d(TAG, "initializeComponents: Servicios inicializados correctamente")
    }
}