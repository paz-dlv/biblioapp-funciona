package com.biblioapp.model

data class Cart(
    val id: Int,
    val user_id: Int,
    val created_at: Long? = null
)
