package com.biblioapp.model


data class CartItem(
    val id: Int,
    val created_at: Long? = null,
    var quantity: Int,
    val cart_id: Int,
    val product_id: Int,
    val product: Product? = null // si tu endpoint devuelve el producto embebido
)