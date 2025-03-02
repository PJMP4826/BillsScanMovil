package com.example.camerax.models

data class Ticket(
    val id: String = System.currentTimeMillis().toString(),
    val empresa: String,
    val fecha: String,
    val hora: String,
    val imageUri: String,
    val detalles: List<DetalleCompra>,
    val total: Double = detalles.sumOf { it.subtotal }
)

data class DetalleCompra(
    val cantidad: Int,
    val descripcion: String,
    val precioUnitario: Double,
    val subtotal: Double
)
