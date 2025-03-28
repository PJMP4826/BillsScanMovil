package com.example.camerax.repositories

import android.content.Context
import android.util.Log
import com.example.camerax.data.TicketDataStore
import com.example.camerax.data.TicketRemoteStore
import com.example.camerax.models.Ticket
import kotlinx.coroutines.flow.*

class TicketRepository(private val context: Context) {
    private val ticketDataStore = TicketDataStore(context)
    private val ticketRemoteStore = TicketRemoteStore()
    private val tickets = MutableStateFlow<List<Ticket>>(emptyList())

    init {
        loadTickets()
    }

    private fun loadTickets() {
        val savedTickets = ticketDataStore.getSavedTickets()
        tickets.value = savedTickets
        Log.d("TicketRepository", "Cargados ${savedTickets.size} tickets desde almacenamiento local")

        ticketRemoteStore.getAllTickets { remoteTickets ->
            tickets.value = remoteTickets
            Log.d("TicketRepository", "Cargados ${remoteTickets.size} tickets desde Firebase")
        }
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
            ticketList.filter { it.empresa.contains(query, ignoreCase = true) }
        }

    suspend fun addTicket(ticket: Ticket) {
        Log.d("TicketRepository", "Añadiendo ticket para: ${ticket.empresa}")

        // Guardar en Firebase
        ticketRemoteStore.saveTicket(ticket)

        // Guardar en local
        ticketDataStore.saveTicketObject(ticket)

        // Actualizar flujo de datos en memoria
        tickets.update { currentList -> listOf(ticket) + currentList }
    }

    fun refreshTickets() {
        Log.d("TicketRepository", "Refrescando tickets desde Firebase")
        ticketRemoteStore.getAllTickets { remoteTickets ->
            tickets.value = remoteTickets
            Log.d("TicketRepository", "Tickets actualizados: ${remoteTickets.size}")
        }
    }
}
