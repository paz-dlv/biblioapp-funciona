package com.biblioapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Modelo User adaptado al JSON que devuelve tu backend.
 * Todos los campos son opcionales con valores por defecto para evitar fallos de parsing.
 */
data class User(
    val id: Int = 0,

    @SerializedName("created_at")
    val createdAt: Long? = null,

    val name: String? = null,

    val lastname: String? = null,

    val email: String? = null,

    val status: String? = null,

    @SerializedName("shipping_address")
    val shippingAddress: String? = null,

    val phone: String? = null,

    val role: String? = null
) : Serializable