package com.example.camerax.data

import android.util.Log
import com.example.camerax.models.Ticket
import com.google.firebase.database.*

class TicketRemoteStore {
    private val database = FirebaseDatabase.getInstance().getReference("tickets")

    fun saveTicket(ticket: Ticket) {
        ticket.calcularTotal() // 🔹 Recalcula el total antes de guardarlo

        database.child(ticket.id).setValue(ticket)
            .addOnSuccessListener { Log.d("TicketRemoteStore", "Ticket guardado en Firebase") }
            .addOnFailureListener { e -> Log.e("TicketRemoteStore", "Error al guardar en Firebase", e) }
    }

    fun getAllTickets(onResult: (List<Ticket>) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ticketList = snapshot.children.mapNotNull {
                    it.getValue(Ticket::class.java)?.apply { calcularTotal() } // 🔹 Recalcula después de leer
                }
                onResult(ticketList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TicketRemoteStore", "Error al recuperar tickets", error.toException())
            }
        })
    }

    fun deleteTicket(ticketId: String) {
        database.child(ticketId).removeValue()
            .addOnSuccessListener { Log.d("TicketRemoteStore", "Ticket eliminado de Firebase") }
            .addOnFailureListener { e -> Log.e("TicketRemoteStore", "Error al eliminar de Firebase", e) }
    }

    fun updateTicket(ticket: Ticket) {
        ticket.calcularTotal()
        database.child(ticket.id).setValue(ticket)
            .addOnSuccessListener { Log.d("TicketRemoteStore", "Ticket actualizado en Firebase") }
            .addOnFailureListener { e -> Log.e("TicketRemoteStore", "Error al actualizar en Firebase", e) }
    }
}
