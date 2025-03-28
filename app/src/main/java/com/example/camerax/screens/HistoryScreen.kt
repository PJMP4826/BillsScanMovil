package com.example.camerax.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camerax.viewmodels.SharedViewModel
import com.example.camerax.models.Ticket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: SharedViewModel) {
    val tickets by viewModel.allTickets.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },

            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F0F0))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    HistoryTableHeader()
                }
                items(tickets) { ticket ->
                    HistoryTableRow(ticket)
                }
            }
        }
    }
}

@Composable
fun HistoryTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2), shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderCell("Empresa", 0.3f)
        HeaderCell("Productos", 0.3f)
        HeaderCell("Fecha y Hora", 0.4f)
    }
}

@Composable
fun HistoryTableRow(ticket: Ticket) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Empresa
                Text(
                    text = ticket.empresa,
                    modifier = Modifier.weight(0.3f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start
                )

                // Mostrar/Ocultar Button
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.weight(0.3f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (expanded) "Ocultar" else "Mostrar",
                            color = Color(0xFF1976D2)
                        )

                    }
                }

                // Fecha y Hora
                Text(
                    text = "${ticket.fecha} ${ticket.hora}",
                    modifier = Modifier.weight(0.4f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                )
            }

            // Expanded Details
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F1F1))
                        .padding(12.dp)
                ) {
                    ticket.detalles.forEach { detalle ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "${detalle.cantidad}x",
                                modifier = Modifier.weight(0.2f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = detalle.descripcion,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .weight(weight)
    )
}