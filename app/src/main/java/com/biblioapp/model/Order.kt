package com.biblioapp.model

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Int,
    val created_at: Long? = null,
    val total: Double? = null,
    val user_id: Int? = null,
    @SerializedName("estado")
    val status: String? = null
    // añade más campos si tu backend devuelve (items, address, etc.)
)