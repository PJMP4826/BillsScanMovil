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
    private val prefs: SharedPreferences = context.getSharedPreferences("ticket_images", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTicket(ticket: TicketResponse, imageUri: Uri) {
        val ticketImages = getTicketImages().toMutableMap()
        ticketImages[ticket.resultado.encabezado.nombre_empresa] = imageUri.toString()
        val json = gson.toJson(ticketImages)
        prefs.edit().putString("ticket_images", json).apply()
    }

    fun getTicketImages(): Map<String, String> {
        val json = prefs.getString("ticket_images", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type)
    }
}
