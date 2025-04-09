package com.example.camerax.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import com.example.camerax.models.TicketResponse
import com.example.camerax.models.DetalleCompra
import com.example.camerax.models.Ticket
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class TicketWithImage(
    val ticket: TicketResponse,
    val imageUri: String
)

class TicketDataStore(context: Context) {
    // Usamos la misma clave que el Repository para que ambos accedan a los mismos datos
    private val prefs: SharedPreferences = context.getSharedPreferences("tickets_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Guardar tanto el TicketResponse como el URI de la imagen
    fun saveTicket(ticket: TicketResponse, imageUri: Uri) {
        try {
            Log.d("TicketDataStore", "Comenzando guardado de ticket: ${imageUri.toString()}")

            // Guardar el mapeo de empresa a URI
            val ticketImages = getTicketImages().toMutableMap()
            ticketImages[ticket.resultado.encabezado.nombre_empresa] = imageUri.toString()
            prefs.edit().putString("ticket_images", gson.toJson(ticketImages)).apply()

            // Crear objeto Ticket para guardar
            val ticketObject = Ticket(
                empresa = ticket.resultado.encabezado.nombre_empresa,
                fecha = ticket.resultado.encabezado.fecha,
                hora = ticket.resultado.encabezado.hora,
                imageUri = imageUri.toString(),
                detalles = ticket.resultado.detalle_compra.map { detalle ->
                    DetalleCompra(
                        cantidad = detalle.cantidad,
                        descripcion = detalle.descripcion,
                        precioUnitario = detalle.precio_unitario,
                        subtotal = detalle.subtotal
                    )
                }
            )

            // Obtener lista actual de tickets y añadir el nuevo
            val ticketsList = getSavedTickets().toMutableList()
            Log.d("TicketDataStore", "Tickets existentes antes de guardar: ${ticketsList.size}")

            // Evitar duplicados comprobando por imageUri
            val existingIndex = ticketsList.indexOfFirst { it.imageUri == imageUri.toString() }
            if (existingIndex != -1) {
                Log.d("TicketDataStore", "Actualizando ticket existente en índice: $existingIndex")
                ticketsList[existingIndex] = ticketObject
            } else {
                Log.d("TicketDataStore", "Añadiendo nuevo ticket")
                ticketsList.add(0, ticketObject)
            }

            // Guardar la lista actualizada
            prefs.edit().putString("saved_tickets", gson.toJson(ticketsList)).apply()

            Log.d("TicketDataStore", "Ticket guardado con éxito. Total tickets: ${ticketsList.size}")
        } catch (e: Exception) {
            Log.e("TicketDataStore", "Error al guardar ticket: ${e.message}", e)
        }
    }

    // Obtener el mapeo de empresa a URI (para compatibilidad)
    fun getTicketImages(): Map<String, String> {
        val json = prefs.getString("ticket_images", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.e("TicketDataStore", "Error al deserializar ticket_images: ${e.message}")
            emptyMap()
        }
    }

    // Obtener la lista completa de tickets guardados
    fun getSavedTickets(): List<Ticket> {
        try {
            val json = prefs.getString("saved_tickets", null)
            if (json.isNullOrEmpty()) {
                Log.d("TicketDataStore", "No hay tickets guardados")
                return emptyList()
            }

            val type = object : TypeToken<List<Ticket>>() {}.type
            return gson.fromJson<List<Ticket>>(json, type).map { ticket ->
                ticket.apply { calcularTotal() }
            }.sortedByDescending { it.id } // Ordenar por ID (más reciente primero)
        } catch (e: Exception) {
            Log.e("TicketDataStore", "Error al recuperar tickets: ${e.message}")
            return emptyList()
        }
    }

    // Guardar un ticket directamente (útil para el Repository)
    fun saveTicketObject(ticket: Ticket) {
        try {
            val ticketsList = getSavedTickets().toMutableList()

            // Evitar duplicados comprobando por imageUri
            val existingIndex = ticketsList.indexOfFirst { it.imageUri == ticket.imageUri }
            if (existingIndex != -1) {
                ticketsList[existingIndex] = ticket
            } else {
                ticketsList.add(0, ticket)
            }

            // Guardar la lista actualizada
            prefs.edit().putString("saved_tickets", gson.toJson(ticketsList)).apply()
            Log.d("TicketDataStore", "Objeto Ticket guardado directamente con éxito")
        } catch (e: Exception) {
            Log.e("TicketDataStore", "Error al guardar objeto Ticket: ${e.message}")
        }
    }

    // Limpiar todos los datos guardados (útil para depuración)
    fun clearAllData() {
        prefs.edit().clear().apply()
        Log.d("TicketDataStore", "Todos los datos borrados")
    }

    fun deleteTicket(ticket: Ticket) {
        try {
            // Obtener la lista actual de tickets
            val ticketsList = getSavedTickets().toMutableList()
            ticketsList.removeIf { it.id == ticket.id }
            
            // Guardar la lista actualizada
            prefs.edit().putString("saved_tickets", gson.toJson(ticketsList)).apply()
            
            // Eliminar la imagen asociada del mapeo
            val ticketImages = getTicketImages().toMutableMap()
            ticketImages.remove(ticket.empresa)
            prefs.edit().putString("ticket_images", gson.toJson(ticketImages)).apply()
            
            Log.d("TicketDataStore", "Ticket eliminado con éxito")
        } catch (e: Exception) {
            Log.e("TicketDataStore", "Error al eliminar ticket: ${e.message}")
        }
    }
}