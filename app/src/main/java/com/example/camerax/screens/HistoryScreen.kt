package com.example.camerax.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.camerax.TicketDataStore
import com.example.camerax.TicketWithImage
import android.net.Uri

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val ticketDataStore = remember { TicketDataStore(context) }
    val tickets = remember { ticketDataStore.getTickets() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tickets) { ticketWithImage ->
            TicketCard(ticketWithImage = ticketWithImage)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCard(ticketWithImage: TicketWithImage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Image
            AsyncImage(
                model = Uri.parse(ticketWithImage.imageUri),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ticket information
            Text(
                text = ticketWithImage.ticket.resultado.encabezado.nombre_empresa,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "Fecha: ${ticketWithImage.ticket.resultado.encabezado.fecha}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Hora: ${ticketWithImage.ticket.resultado.encabezado.hora}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            ticketWithImage.ticket.resultado.detalle_compra.forEach { detalle ->
                Text(
                    text = "${detalle.cantidad} x ${detalle.descripcion} - \$${detalle.subtotal}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
