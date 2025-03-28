package com.example.camerax.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camerax.viewmodels.SharedViewModel
import com.example.camerax.models.Ticket

@Composable
fun CategoriesScreen(viewModel: SharedViewModel) {
    val categorizedTickets by viewModel.categorizedTickets.collectAsState(initial = emptyMap())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categorizedTickets.forEach { (category, tickets) ->
            item {
                CategorySection(category, tickets)
            }
        }
    }
}

@Composable
fun CategorySection(category: String, tickets: List<Ticket>) {
    val groupedTickets = tickets.groupBy { it.empresa }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        groupedTickets.forEach { (empresa, tickets) ->
            val totalTickets = tickets.size
            val totalAmount = tickets.sumOf { it.total }
            val ticketText = if (totalTickets == 1) "1 Ticket" else "$totalTickets Tickets"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2D5D89))
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = empresa,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalAmount MXN",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        text = ticketText,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
