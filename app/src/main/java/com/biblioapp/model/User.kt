package com.biblioapp.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val created_at: Long? = null,
    val name: String? = null,
    val email: String? = null,
    val lastname: String? = null,
    val status: String? = null,
    @SerializedName("shipping_address")
    val shippingAddress: String? = null,
    val phone: String? = null,
    val role: String? = null
)