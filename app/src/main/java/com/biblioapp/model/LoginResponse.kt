package com.biblioapp.model

data class LoginResponse(
    val user: User,
    val authToken: String
)
