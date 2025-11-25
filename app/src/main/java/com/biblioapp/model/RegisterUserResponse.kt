package com.biblioapp.model

data class RegisterUserResponse(
    val authToken: String,
    val user: User

)
