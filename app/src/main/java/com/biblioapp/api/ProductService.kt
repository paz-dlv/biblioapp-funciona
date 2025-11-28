package com.biblioapp.api

import com.biblioapp.model.CreateProductRequest
import com.biblioapp.model.CreateProductResponse
import com.biblioapp.model.Product
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductService {
    @GET("product")
    suspend fun getProducts(): List<Product>

    @GET("product/{product_id}")
    suspend fun getProduct(@Path("product_id") id: Int): Product

    // Creación con request tipado (ajusta si tu backend devuelve Product en vez de wrapper)
    @POST("product")
    suspend fun createProduct(@Body body: CreateProductRequest): CreateProductResponse

    @PATCH("product/{product_id}")
    suspend fun updateProduct(@Path("product_id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any>): Product

    @DELETE("product/{product_id}")
    suspend fun deleteProduct(@Path("product_id") id: Int)
}