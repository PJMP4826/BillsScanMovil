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
import com.example.camerax.models.Ticket

@Composable
fun HistoryScreen(viewModel: SharedViewModel) {
    val tickets by viewModel.allTickets.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0))
            .padding(horizontal = 16.dp)
    ) {
        // Título de la pantalla
        Text(
            text = "Historial",
            modifier = Modifier
                .padding(top = 16.dp, bottom = 8.dp)
                .align(Alignment.Start),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(tickets) { ticket ->
                HistoryTicketCard(ticket)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTicketCard(ticket: Ticket) {
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
            // Imagen del ticket
            AsyncImage(
                model = Uri.parse(ticket.imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentScale = ContentScale.Crop
            )

            // Información del ticket
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

                TicketDetail(label = "Fecha:", value = ticket.fecha)
                TicketDetail(label = "Hora:", value = ticket.hora)

                Spacer(modifier = Modifier.height(4.dp))

                // Lista de productos
                Text(
                    text = "Productos:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                ticket.detalles.forEach { detalle ->
                    Text(
                        text = "${detalle.cantidad}x ${detalle.descripcion}",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TicketDetail(label = "Total:", value = "$${ticket.total}")
            }
        }
    }
}

// Componente reutilizable para detalles del ticket
@Composable
fun TicketDetail(label: String, value: String) {
    Text(
        text = "$label $value",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.Black
    )
}
