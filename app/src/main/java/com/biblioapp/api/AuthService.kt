package com.biblioapp.api

import android.telecom.Call
import com.biblioapp.model.AuthResponse
import com.biblioapp.model.LoginRequest
import com.biblioapp.model.RegisterUserRequest
import com.biblioapp.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET

import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun getMe(): User

    @POST("auth/signup")
    suspend fun signUp(@Body request: RegisterUserRequest): Response<User>
}




