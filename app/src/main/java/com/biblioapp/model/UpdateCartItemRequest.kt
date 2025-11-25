package com.biblioapp.model

data class UpdateCartItemRequest(
    val quantity: Int? = null,
    val product_id: Int? = null,
    val cart_id: Int? = null
)
