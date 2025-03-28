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
        repository.searchTickets(query)
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
}
