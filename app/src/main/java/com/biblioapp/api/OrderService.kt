package com.biblioapp.api

import com.biblioapp.model.Order
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderService {
    @GET("order")
    suspend fun getOrders(): List<Order>

    @GET("order/{order_id}")
    suspend fun getOrder(@Path("order_id") id: Int): Order

    @POST("order")
    suspend fun createOrder(@Body body: Map<String, Any>): Order

    /**
     * Actualiza campos del pedido (por ejemplo: estado = "ACEPTADA" / "RECHAZADA" / "PENDIENTE")
     * El body puede ser algo como mapOf("estado" to "ACEPTADA")
     */
    @PATCH("order/{order_id}")
    suspend fun updateOrder(@Path("order_id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Order
}