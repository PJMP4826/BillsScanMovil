package com.example.camerax.viewmodels

import android.net.Uri
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTicket(ticket: Ticket) {
        viewModelScope.launch {
            repository.addTicket(ticket)
        }
    }

    fun addNewTicket(ticket: Ticket) {
        viewModelScope.launch {
            repository.addTicket(ticket)
        }
    }
}
