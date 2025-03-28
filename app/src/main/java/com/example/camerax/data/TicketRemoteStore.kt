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


}
