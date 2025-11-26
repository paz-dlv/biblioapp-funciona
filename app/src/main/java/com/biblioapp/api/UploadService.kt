package com.biblioapp.api

import com.biblioapp.model.ProductImage
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService { // Declaramos una interfaz de Retrofit

    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(

        @Part image: MultipartBody.Part

    ): List<ProductImage>
}