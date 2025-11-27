package com.biblioapp.api

import com.biblioapp.model.CreateProductRequest
import com.biblioapp.model.CreateProductResponse
import com.biblioapp.model.Product
import com.biblioapp.model.UpdateProductRequest
import retrofit2.Response
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

    @POST("product")
    suspend fun createProduct(@Body request: CreateProductRequest): CreateProductResponse

    @PATCH("product/{product_id}")
    suspend fun updateProduct(@Path("product_id") productId: Int, @Body request: UpdateProductRequest): Product

    @DELETE("product/{product_id}")
    suspend fun deleteProduct(@Path("product_id") productId: Int): Response<Unit>
}