package com.biblioapp.api

import com.biblioapp.model.CreateProductRequest
import com.biblioapp.model.CreateProductResponse
import com.biblioapp.model.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProductService {
    @GET("product")
    suspend fun getProducts(): List<Product>

    @POST("product")
    suspend fun createProduct(@Body request: CreateProductRequest): CreateProductResponse



}