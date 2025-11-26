package com.biblioapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("id") val id: Int,

    @SerializedName("created_at") val createdAt: Long?,

    @SerializedName("name") val name: String?,

    @SerializedName("email") val email: String?,

    @SerializedName("lastname") val lastname: String?,

    @SerializedName("status") val status: String?,

    @SerializedName("shipping_address") val shippingAddress: String?,

    @SerializedName("phone") val phone: String?,

    @SerializedName("role") val role: String?
) : Serializable