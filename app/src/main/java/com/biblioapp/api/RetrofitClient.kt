package com.biblioapp.api

import android.content.Context
import com.biblioapp.api.ApiConfig.authBaseUrl
import com.biblioapp.api.ApiConfig.storeBaseUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient - fábricas de servicios Retrofit reutilizables.
 *
 * Incluye:
 *  - baseOkHttpBuilder(): builder base con logging y timeouts
 *  - retrofit(baseUrl, client): crea instancia Retrofit con Gson
 *  - createAuthService/createProductService/createUploadService/createOrderService/createCartService/createUserService
 *
 * Ajusta los nombres de base URL en ApiConfig si es necesario.
 */
object RetrofitClient {

    // Builder base de OkHttp configurado con logging y timeouts.
    private fun baseOkHttpBuilder(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
    }

    // Construye Retrofit con la baseUrl y el cliente OkHttp proporcionado.
    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /**
     * Crea AuthService.
     * @param context Context (posible uso para TokenManager si requiresAuth = true)
     * @param requiresAuth si true se añade AuthInterceptor con token
     */
    fun createAuthService(context: Context, requiresAuth: Boolean = false): AuthService {
        val clientBuilder = baseOkHttpBuilder()
        if (requiresAuth) {
            val tokenManager = TokenManager(context)
            clientBuilder.addInterceptor(AuthInterceptor { tokenManager.getToken() })
        }
        val client = clientBuilder.build()
        return retrofit(authBaseUrl, client).create(AuthService::class.java)
    }

    /**
     * ProductService con Authorization (token en header).
     */
    fun createProductService(context: Context): ProductService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        return retrofit(storeBaseUrl, client).create(ProductService::class.java)
    }

    /**
     * UploadService (usa token)
     */
    fun createUploadService(context: Context): UploadService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        return retrofit(storeBaseUrl, client).create(UploadService::class.java)
    }

    /**
     * OrderService (usa token)
     */
    fun createOrderService(context: Context): OrderService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        return retrofit(storeBaseUrl, client).create(OrderService::class.java)
    }

    /**
     * CartService (usa token)
     */
    fun createCartService(context: Context): CartService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        return retrofit(storeBaseUrl, client).create(CartService::class.java)
    }

    /**
     * UserService (usa token)
     */
    fun createUserService(context: Context): UserService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        return retrofit(storeBaseUrl, client).create(UserService::class.java)
    }
}