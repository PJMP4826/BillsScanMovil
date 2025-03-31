package com.example.camerax.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.activity.ComponentActivity
import androidx.navigation.NavHostController
import com.example.camerax.R
import com.example.camerax.config.TicketApiService
import com.example.camerax.data.TicketDataStore
import com.example.camerax.screens.*
import com.example.camerax.viewmodels.AuthViewModel
import com.example.camerax.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

sealed class Screen {
    object Dashboard : Screen()
    object History : Screen()
    object Categories : Screen()
    object Tickets : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viewModel: SharedViewModel,
    apiService: TicketApiService,
    ticketDataStore: TicketDataStore,
    lifecycleScope: LifecycleCoroutineScope,
    activity: ComponentActivity
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "BillsScan",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1352A5)
                )
                Divider()
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color(0xFF1352A5)
                        )
                    },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("BillsScan") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1352A5),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF2A4D8A)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Dashboard,
                        onClick = { currentScreen = Screen.Dashboard },
                        label = {
                            Text(
                                "Inicio",
                                fontWeight = if (currentScreen == Screen.Dashboard) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.home2),
                                contentDescription = "Inicio",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFDAAA3F)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.History,
                        onClick = { currentScreen = Screen.History },
                        label = {
                            Text(
                                "Historial",
                                fontWeight = if (currentScreen == Screen.History) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.history),
                                contentDescription = "Historial",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFDAAA3F)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Categories,
                        onClick = { currentScreen = Screen.Categories },
                        label = {
                            Text(
                                "Grupo",
                                fontWeight = if (currentScreen == Screen.Categories) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.categorias),
                                contentDescription = "Grupo",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFDAAA3F)
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Tickets,
                        onClick = { currentScreen = Screen.Tickets },
                        label = {
                            Text(
                                "Tickets",
                                fontWeight = if (currentScreen == Screen.Tickets) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.bills),
                                contentDescription = "Tickets",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFFDAAA3F),
                            unselectedIconColor = Color.Black,
                            unselectedTextColor = Color.Black,
                            indicatorColor = Color(0xFFDAAA3F)
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
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
}