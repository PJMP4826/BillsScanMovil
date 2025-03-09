package com.example.camerax

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationController(navHostController: NavHostController, viewModel: AuthViewModel){
    NavHost(navController = navHostController, startDestination = "login") {
        composable("login"){
            Auth(
                navHostController, viewModel
            )
        }
        composable("registro"){
            UserRegister(
                navHostController, viewModel
            )
        }
        composable("pantallaInicio"){}
    }
}