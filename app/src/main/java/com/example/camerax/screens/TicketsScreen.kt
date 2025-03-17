package com.example.camerax.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.camerax.viewmodels.SharedViewModel

@Composable
fun TicketsScreen(viewModel: SharedViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0)) // Fondo gris como en la imagen
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = "Tickets",
            modifier = Modifier
                .padding(top = 16.dp, bottom = 8.dp)
                .align(Alignment.Start),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Barra de búsqueda con botón
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de búsqueda
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                placeholder = { Text("", color = Color.Black) },
                singleLine = true
            )

            // Botón de búsqueda
            Button(
                onClick = { viewModel.updateSearchQuery(searchQuery) },
                modifier = Modifier
                    .height(48.dp)
                    .width(100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDAAA3F))
            ) {
                Text(
                    text = "Buscar",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Lista de tickets
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(searchResults) { ticket ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        // Imagen del ticket con mayor altura
                        AsyncImage(
                            model = Uri.parse(ticket.imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp), // Increased from 200dp to 250dp
                            contentScale = ContentScale.Crop
                        )

                        // Datos del ticket con fondo blanco
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = ticket.empresa,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black
                            )

                            // Fecha y hora con formato como en la imagen
                            Text(
                                text = "Fecha: ${ticket.fecha}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )

                            Text(
                                text = "Hora: ${ticket.hora}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Productos
                            Text(
                                text = "Productos:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )

                            // Lista de productos
                            ticket.detalles.forEach { detalle ->
                                Text(
                                    text = "${detalle.cantidad}x ${detalle.descripcion}",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Total
                            Text(
                                text = "Total: $${ticket.total}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}