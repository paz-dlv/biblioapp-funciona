package com.biblioapp.api

import com.biblioapp.model.Cart
import com.biblioapp.model.CartItem
import com.biblioapp.model.CreateCartItemRequest
import com.biblioapp.model.CreateCartRequest
import com.biblioapp.model.UpdateCartItemRequest
import com.biblioapp.model.UpdateCartRequest
import retrofit2.Response
import retrofit2.http.*

interface CartService {

    // CART endpoints
    @GET("cart")
    suspend fun getCarts(@Query("user_id") userId: Int? = null): List<Cart>

    @POST("cart")
    suspend fun createCart(@Body body: CreateCartRequest): Cart

    @GET("cart/{cart_id}")
    suspend fun getCart(@Path("cart_id") cartId: Int): Cart

    @PATCH("cart/{cart_id}")
    suspend fun updateCart(@Path("cart_id") cartId: Int, @Body body: UpdateCartRequest): Cart

    @DELETE("cart/{cart_id}")
    suspend fun deleteCart(@Path("cart_id") cartId: Int): Response<Unit>

    // CART_ITEM endpoints
    @GET("cart_item")
    suspend fun getCartItems(@Query("cart_id") cartId: Int? = null): List<CartItem>

    @POST("cart_item")
    suspend fun createCartItem(@Body body: CreateCartItemRequest): CartItem

    @GET("cart_item/{cart_item_id}")
    suspend fun getCartItem(@Path("cart_item_id") id: Int): CartItem

    @PATCH("cart_item/{cart_item_id}")
    suspend fun updateCartItem(@Path("cart_item_id") id: Int, @Body body: UpdateCartItemRequest): CartItem

    @DELETE("cart_item/{cart_item_id}")
    suspend fun deleteCartItem(@Path("cart_item_id") id: Int): Response<Unit>
}