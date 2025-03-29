package com.example.camerax.models

data class TicketResponse(val resultado: TicketResult)

data class TicketResult(
    val detalle_compra: List<CompraDetalle>,
    val encabezado: Encabezado
)

data class CompraDetalle(
    val cantidad: Int,
    val descripcion: String,
    val precio_unitario: Double,
    val subtotal: Double
)

data class Encabezado(
    val nombre_empresa: String,
    val fecha: String,
    val hora: String
)