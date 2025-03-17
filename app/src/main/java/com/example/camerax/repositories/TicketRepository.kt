package com.example.camerax.repositories

import android.content.Context
import android.util.Log
import com.example.camerax.TicketDataStore
import com.example.camerax.models.Ticket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TicketRepository(private val context: Context) {
    private val ticketDataStore = TicketDataStore(context)
    private val tickets = MutableStateFlow<List<Ticket>>(loadTickets())

    private fun loadTickets(): List<Ticket> {
        val savedTickets = ticketDataStore.getSavedTickets()
        Log.d("TicketRepository", "Cargados ${savedTickets.size} tickets desde TicketDataStore")
        return savedTickets
    }

    fun getAllTickets(): Flow<List<Ticket>> = tickets

    fun getRecentTickets(limit: Int): Flow<List<Ticket>> =
        tickets.map { it.take(limit) }

    fun getTicketsByCategory(): Flow<Map<String, List<Ticket>>> =
        tickets.map { ticketList ->
            ticketList.groupBy { it.empresa }
        }

    fun searchTickets(query: String): Flow<List<Ticket>> =
        tickets.map { ticketList ->
            ticketList.filter {
                it.empresa.contains(query, ignoreCase = true)
            }
        }

    suspend fun addTicket(ticket: Ticket) {
        Log.d("TicketRepository", "Añadiendo ticket para: ${ticket.empresa}")

        // Actualizar el StateFlow
        tickets.update { currentList ->
            val mutableList = currentList.toMutableList()

            // Evitar duplicados
            val existingIndex = mutableList.indexOfFirst { it.imageUri == ticket.imageUri }
            if (existingIndex != -1) {
                Log.d("TicketRepository", "Actualizando ticket existente")
                mutableList[existingIndex] = ticket
            } else {
                Log.d("TicketRepository", "Añadiendo nuevo ticket")
                mutableList.add(0, ticket)
            }
            mutableList
        }

        // Guardar en el TicketDataStore también
        ticketDataStore.saveTicketObject(ticket)
    }

    // Método para recargar tickets desde el TicketDataStore
    fun refreshTickets() {
        Log.d("TicketRepository", "Refrescando tickets desde almacenamiento")
        val loadedTickets = ticketDataStore.getSavedTickets()
        tickets.value = loadedTickets
        Log.d("TicketRepository", "Tickets refrescados: ${loadedTickets.size}")
    }
}