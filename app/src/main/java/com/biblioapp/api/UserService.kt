package com.biblioapp.api

import com.biblioapp.model.User
import retrofit2.http.*

interface UserService {
    @GET("user")
    suspend fun getUsers(): List<User>

    @GET("user/{id}")
    suspend fun getUser(@Path("id") id: Int): User

    @POST("user")
    suspend fun createUser(@Body body: Map<String, @JvmSuppressWildcards Any>): User

    // PATCH para actualizar campos parciales (status, shipping_address, etc.)
    @PATCH("user/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): User

    @DELETE("user/{id}")
    suspend fun deleteUser(@Path("id") id: Int)
}