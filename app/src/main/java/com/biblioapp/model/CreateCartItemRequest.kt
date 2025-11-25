package com.biblioapp.model

data class CreateCartItemRequest(
    val cart_id: Int,
    val product_id: Int,
    val quantity: Int
)