package com.biblioapp.api

import com.biblioapp.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.DELETE

interface UserService {
    @GET("user")
    suspend fun getUsers(): List<User>

    @GET("user/{user_id}")
    suspend fun getUser(@Path("user_id") id: Int): User

    // Actualiza completamente (si tu endpoint usa PUT)
    @PUT("user/{user_id}")
    suspend fun putUser(@Path("user_id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any?>): User

    // Patch parcial (acepta campos parciales)
    @PATCH("user/{user_id}")
    suspend fun patchUser(@Path("user_id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any?>): User

    @DELETE("user/{user_id}")
    suspend fun deleteUser(@Path("user_id") id: Int): Response<Unit>
}