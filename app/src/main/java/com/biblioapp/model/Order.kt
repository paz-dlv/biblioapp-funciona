package com.biblioapp.model

data class Order(
    val id: Int,
    val user_id: Int?,
    val total: Double?,
    val status: String?,
    val created_at: Long? = null
)