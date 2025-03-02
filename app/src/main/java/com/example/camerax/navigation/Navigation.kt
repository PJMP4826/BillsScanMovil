package com.example.camerax.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.camerax.screens.*
import com.example.camerax.viewmodels.SharedViewModel
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.camerax.TicketApiService
import com.example.camerax.TicketDataStore

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
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.Dashboard,
                    onClick = { currentScreen = Screen.Dashboard },
                    label = { Text(text = "Home") },
                    icon = { }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.History,
                    onClick = { currentScreen = Screen.History }, // Corregido aquí
                    label = { Text(text = "History") },
                    icon = { }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Categories,
                    onClick = { currentScreen = Screen.Categories }, // Corregido aquí
                    label = { Text(text = "Categories") },
                    icon = { }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.Tickets,
                    onClick = { currentScreen = Screen.Tickets }, // Corregido aquí
                    label = { Text(text = "Tickets") },
                    icon = { }
                )
            }
        }
    ) { innerPadding -> 
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    apiService = apiService,
                    ticketDataStore = ticketDataStore,
                    lifecycleScope = lifecycleScope,
                    activity = activity
                )
                Screen.History -> HistoryScreen(viewModel)
                Screen.Categories -> CategoriesScreen(viewModel)
                Screen.Tickets -> TicketsScreen(viewModel)
            }
        }
    }
}
