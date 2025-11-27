package com.biblioapp.api

import com.biblioapp.model.CreateOrderRequest
import com.biblioapp.model.Order
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderService {
    @POST("order")
    suspend fun createOrder(@Body body: CreateOrderRequest): Order
}