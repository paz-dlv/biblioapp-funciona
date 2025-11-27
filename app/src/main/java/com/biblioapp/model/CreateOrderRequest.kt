package com.biblioapp.model

data class CreateOrderRequest(
    val user_id: Int?,
    val total: Double?,
    // opcional: puedes enviar items si tu backend lo requiere
    val items: List<OrderItemRequest>? = null
)

data class OrderItemRequest(
    val product_id: Int,
    val quantity: Int
)