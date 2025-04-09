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
        // Primero cargar datos locales
        val savedTickets = ticketDataStore.getSavedTickets()
        tickets.value = savedTickets
        Log.d("TicketRepository", "Cargados ${savedTickets.size} tickets desde almacenamiento local")

        // Luego cargar datos de Firebase y combinar
        ticketRemoteStore.getAllTickets { remoteTickets ->
            val combinedTickets = (remoteTickets + savedTickets)
                .distinctBy { it.id }  // Eliminar duplicados
                .sortedByDescending { it.id }  // Ordenar por más reciente
            tickets.value = combinedTickets
            
            // Sincronizar con almacenamiento local
            combinedTickets.forEach { ticket ->
                ticketDataStore.saveTicketObject(ticket)
            }
            Log.d("TicketRepository", "Sincronizados ${combinedTickets.size} tickets")
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

    suspend fun deleteTicket(ticket: Ticket) {
        // Eliminar de Firebase
        ticketRemoteStore.deleteTicket(ticket.id)

        // Eliminar de local storage
        ticketDataStore.deleteTicket(ticket)

        // Eliminar de la memoria
        val currentTickets = tickets.value.toMutableList()
        currentTickets.remove(ticket)
        tickets.value = currentTickets

        Log.d("TicketRepository", "Ticket eliminado completamente")
    }

    suspend fun updateTicket(ticket: Ticket) {
        // Actualizar en Firebase
        ticketRemoteStore.updateTicket(ticket)

        // Actualizar en memoria
        val currentTickets = tickets.value.toMutableList()
        val index = currentTickets.indexOfFirst { it.id == ticket.id }
        if (index != -1) {
            currentTickets[index] = ticket
            tickets.value = currentTickets
        }

        // Actualizar local storage
        ticketDataStore.saveTicketObject(ticket)
    }

    fun refreshTickets() {
        Log.d("TicketRepository", "Refrescando tickets")
        val localTickets = ticketDataStore.getSavedTickets()
        
        ticketRemoteStore.getAllTickets { remoteTickets ->
            val combinedTickets = (remoteTickets + localTickets)
                .distinctBy { it.id }
                .sortedByDescending { it.id }
            tickets.value = combinedTickets
            
            // Actualizar almacenamiento local
            combinedTickets.forEach { ticket ->
                ticketDataStore.saveTicketObject(ticket)
            }
            Log.d("TicketRepository", "Tickets actualizados: ${combinedTickets.size}")
        }
    }
}
