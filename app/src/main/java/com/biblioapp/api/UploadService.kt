package com.biblioapp.api

import com.biblioapp.model.ProductImage
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService { // Declaramos una interfaz de Retrofit

    @Multipart // Indicamos que la solicitud será multipart/form-data
    @POST("upload/image") // Ruta del endpoint de subida (POST /upload/image)
    suspend fun uploadImage( // Función suspend (corrutina) para subir la imagen
        // El nombre del campo "content" en createFormData es el que Xano espera.
        // Aquí el nombre del parámetro 'image' no importa.
        @Part image: MultipartBody.Part
        // ¡¡¡CAMBIO CLAVE!!!
        // La API devuelve un Array (una lista) de objetos de imagen, no un solo objeto.
        // Cambiamos el tipo de retorno de 'UploadResponse' a 'List<ProductImage>'.
    ): List<ProductImage>
}