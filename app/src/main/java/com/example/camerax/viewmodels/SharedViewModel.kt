package com.example.camerax.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.camerax.models.Ticket
import com.example.camerax.repositories.TicketRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SharedViewModel(private val repository: TicketRepository) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val recentTickets = repository.getRecentTickets(5)
    val allTickets = repository.getAllTickets()
    val categorizedTickets = repository.getTicketsByCategory()

    val searchResults = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllTickets()
        } else {
            flow {
                val allTicketsList = repository.getAllTickets().first()
                val filteredTickets = allTicketsList.filter { ticket ->
                    ticket.empresa.contains(query, ignoreCase = true) ||
                            ticket.detalles.any {
                                it.descripcion.contains(query, ignoreCase = true)
                            }
                }
                emit(filteredTickets)
            }
        }
    }

    init {
        refreshTickets()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNewTicket(ticket: Ticket) {
        viewModelScope.launch {
            repository.addTicket(ticket)
        }
    }

    fun refreshTickets() {
        repository.refreshTickets()
    }

    fun deleteTicket(ticket: Ticket) {
        viewModelScope.launch {
            repository.deleteTicket(ticket)
        }
    }

    fun updateTicket(ticket: Ticket) {
        viewModelScope.launch {
            repository.updateTicket(ticket)
        }
    }
}