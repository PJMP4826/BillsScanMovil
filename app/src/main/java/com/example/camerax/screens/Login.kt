package com.example.camerax.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.camerax.R
import com.example.camerax.viewmodels.AuthViewModel
import com.example.camerax.ui.theme.JostFont

@Composable
fun Auth(navHostController: NavHostController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Observar estados del ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Colores personalizados para BillsScan
    val azulBillsScan = Color(0xFF1A4B8C)
    val doradoBillsScan = Color(0xFFE9B84F)

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con la imagen de documentos/facturas
        Image(
            painter = painterResource(id = R.drawable.fondo_imagen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tarjeta de login similar a la imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(
                    containerColor = azulBillsScan
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo (icono de helado) y título
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.billscan_logo), // Asegúrate de tener este recurso
                            contentDescription = "Logo BillsScan",
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BillsScan",
                            color = Color.White,
                            fontSize = 37.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JostFont
                        )
                    }

                    // Campo de correo electrónico
                    Text(
                        text = "Correo Electrónico",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        placeholder = { Text("Ingrese su correo") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )

                    // Campo de contraseña
                    Text(
                        text = "Contraseña",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        placeholder = { Text("Ingrese su contraseña") },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )

                    // Mostrar error si existe
                    error?.let { errorMessage ->
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Botón de ingreso
                    Button(
                        onClick = {
                            viewModel.signIn(
                                context = context,
                                email = email,
                                password = password
                            ) {
                                navHostController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = doradoBillsScan
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Ingresar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Enlace para registrarse
                    TextButton(
                        onClick = { navHostController.navigate("registro") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "¿No tienes cuenta? Regístrate",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}