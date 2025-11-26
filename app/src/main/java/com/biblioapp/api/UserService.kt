package com.biblioapp.api

import com.biblioapp.model.User
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("user")
    suspend fun getUsers(): List<User>

    @GET("user/{user_id}")
    suspend fun getUser(@Path("user_id") id: Int): User
}