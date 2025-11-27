package com.biblioapp.model

import com.google.gson.annotations.SerializedName

data class CartItem(
    val id: Int,
    val created_at: Long? = null,
    var quantity: Int,
    val cart_id: Int,
    val product_id: Int,
    val product: Product? = null, // si tu endpoint devuelve el producto embebido en "product"
    @SerializedName("_product")
    val _product: List<Product>? = null // Xano devuelve a veces "_product": [ {...} ]
)