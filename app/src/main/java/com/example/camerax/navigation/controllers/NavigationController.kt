package com.example.camerax.navigation.controllers

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.activity.ComponentActivity
import com.example.camerax.screens.*
import com.example.camerax.navigation.AppNavigation
import com.example.camerax.viewmodels.AuthViewModel
import com.example.camerax.viewmodels.SharedViewModel
import com.example.camerax.config.TicketApiService
import com.example.camerax.data.TicketDataStore

@Composable
fun NavigationController(
    navHostController: NavHostController,
    authViewModel: AuthViewModel,
    viewModel: SharedViewModel,
    apiService: TicketApiService,
    ticketDataStore: TicketDataStore,
    lifecycleScope: LifecycleCoroutineScope,
    activity: ComponentActivity
) {
    NavHost(
        navController = navHostController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navHostController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            Auth(navHostController, authViewModel)
        }

        composable("registro") {
            UserRegister(navHostController, authViewModel)
        }

        composable("dashboard") {
            AppNavigation(
                navController = navHostController,
                authViewModel = authViewModel,
                viewModel = viewModel,
                apiService = apiService,
                ticketDataStore = ticketDataStore,
                lifecycleScope = lifecycleScope,
                activity = activity
            )
        }
    }
}