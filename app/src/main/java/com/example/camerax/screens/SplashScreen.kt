package com.example.camerax.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camerax.R
import kotlinx.coroutines.delay

val JostFont = FontFamily(
    Font(R.font.jost_regular, FontWeight.Normal),
    Font(R.font.jost_bold, FontWeight.Bold)
)

@Composable
fun SplashScreen(navigateToDashboard: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000) // Espera 3 segundos antes de navegar
        navigateToDashboard() // Navega después de 3 segundos
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1352A5)), // Azul oscuro de fondo
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.billscan_logo), // Asegúrate de que el logo esté en res/drawable
                contentDescription = "BillsScan Logo",
                modifier = Modifier
                    .height(200.dp)
                    .width(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BillsScan",
                color = Color.White,
                fontSize = 50.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontFamily = JostFont
            )
        }
    }
}