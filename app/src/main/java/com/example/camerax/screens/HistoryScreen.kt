package com.example.camerax.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.camerax.viewmodels.SharedViewModel
import com.example.camerax.models.Ticket

@Composable
fun HistoryScreen(viewModel: SharedViewModel) {
    val tickets by viewModel.allTickets.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tickets) { ticket ->
            HistoryTicketCard(ticket)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTicketCard(ticket: Ticket) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Imagen del ticket
            AsyncImage(
                model = Uri.parse(ticket.imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del ticket
            Text(
                text = ticket.empresa,
                style = MaterialTheme.typography.titleLarge
            )
            
            Text(
                text = "Fecha: ${ticket.fecha}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = "Hora: ${ticket.hora}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Detalles de la compra
            ticket.detalles.forEach { detalle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${detalle.cantidad}x ${detalle.descripcion}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$${detalle.subtotal}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Total
            Text(
                text = "Total: $${ticket.total}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
