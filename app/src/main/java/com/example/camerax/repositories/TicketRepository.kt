package com.example.camerax.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.camerax.models.Ticket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TicketRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tickets_db", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val tickets = MutableStateFlow<List<Ticket>>(loadTickets())

    private fun loadTickets(): List<Ticket> {
        val json = prefs.getString("saved_tickets", null)
        return if (json != null) {
            val type = object : TypeToken<List<Ticket>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun saveTickets(ticketList: List<Ticket>) {
        val json = gson.toJson(ticketList)
        prefs.edit().putString("saved_tickets", json).apply()
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
        val currentList = tickets.value.toMutableList()
        currentList.add(0, ticket)
        tickets.emit(currentList)
        saveTickets(currentList)
    }
}
