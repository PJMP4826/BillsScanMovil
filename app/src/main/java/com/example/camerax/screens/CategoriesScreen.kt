package com.example.camerax.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        tickets.forEach { ticket ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Fecha: ${ticket.fecha}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Total: $${ticket.total}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
