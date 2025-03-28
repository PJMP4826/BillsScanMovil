package com.example.camerax.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Ticket(
    var id: String = System.currentTimeMillis().toString(),
    var empresa: String = "",
    var fecha: String = "",
    var hora: String = "",
    var imageUri: String = "",
    var detalles: List<DetalleCompra> = emptyList(),
    var total: Double = 0.0
) {
    fun calcularTotal() {
        total = detalles.sumOf { it.subtotal }
    }
}

@IgnoreExtraProperties
data class DetalleCompra(
    var cantidad: Int = 0,
    var descripcion: String = "",
    var precioUnitario: Double = 0.0,
    var subtotal: Double = 0.0
)
