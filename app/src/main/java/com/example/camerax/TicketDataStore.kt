package com.example.camerax

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class TicketWithImage(
    val ticket: TicketResponse,
    val imageUri: String
)

class TicketDataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tickets", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTicket(ticket: TicketResponse, imageUri: Uri) {
        val tickets = getTickets().toMutableList()
        tickets.add(0, TicketWithImage(ticket, imageUri.toString()))
        val json = gson.toJson(tickets)
        prefs.edit().putString("saved_tickets", json).apply()
    }

    fun getTickets(): List<TicketWithImage> {
        val json = prefs.getString("saved_tickets", null) ?: return emptyList()
        val type = object : TypeToken<List<TicketWithImage>>() {}.type
        return gson.fromJson(json, type)
    }
}
