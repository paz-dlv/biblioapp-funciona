package com.biblioapp.api

import android.content.Context
import com.biblioapp.api.ApiConfig.authBaseUrl
import com.biblioapp.api.ApiConfig.storeBaseUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient { // Objeto singleton que expone métodos de fábrica

    // Builder base de OkHttp configurado con logging y timeouts. No necesita cambios.
    private fun baseOkHttpBuilder(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply { // Creamos el interceptor de logging
            // Nivel BODY útil en desarrollo para ver requests y responses completas.
            level = HttpLoggingInterceptor.Level.BODY // Establecemos el nivel de detalle
        }
        return OkHttpClient.Builder() // Iniciamos el builder de OkHttp
            .addInterceptor(logging) // Añadimos el interceptor de logging
            .connectTimeout(30, TimeUnit.SECONDS) // Timeout de conexión
            .readTimeout(30, TimeUnit.SECONDS) // Timeout de lectura
            .writeTimeout(30, TimeUnit.SECONDS) // Timeout de escritura
    }

    // Función que construye Retrofit con baseUrl y cliente. No necesita cambios.
    private fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder() // Iniciamos builder de Retrofit
            .baseUrl(baseUrl) // Establecemos base URL
            .client(client) // Asociamos cliente OkHttp
            .addConverterFactory(GsonConverterFactory.create()) // Añadimos convertidor Gson
            .build() // Construimos instancia Retrofit

    /**
     * ¡FUNCIÓN MODIFICADA Y UNIFICADA!
     * Fábrica para AuthService. Ahora acepta un parámetro 'requiresAuth'.
     *
     * @param context El contexto de la aplicación.
     * @param requiresAuth Si es 'true', se creará un cliente con el interceptor de token.
     *                     Si es 'false' (por defecto), se creará un cliente público sin token.
     * @return Una instancia de AuthService.
     */
    fun createAuthService(context: Context, requiresAuth: Boolean = false): AuthService {
        val clientBuilder = baseOkHttpBuilder() // Partimos del builder base

        if (requiresAuth) {
            // Si se requiere autenticación, obtenemos el token y añadimos el interceptor
            val tokenManager = TokenManager(context)
            clientBuilder.addInterceptor(AuthInterceptor { tokenManager.getToken() })
        }

        // Construimos el cliente OkHttp y luego la instancia de Retrofit
        val client = clientBuilder.build()
        return retrofit(authBaseUrl, client).create(AuthService::class.java)
    }

    // Fábrica para ProductService (con Authorization). No necesita cambios.
    fun createProductService(context: Context): ProductService {
        val tokenManager = TokenManager(context) // Acceso al TokenManager para obtener el token
        val client = baseOkHttpBuilder() // Partimos del builder base
            .addInterceptor(AuthInterceptor { tokenManager.getToken() }) // Añadimos nuestro interceptor que inserta Bearer token
            .build() // Construimos cliente OkHttp
        return retrofit(storeBaseUrl, client).create(ProductService::class.java) // Construimos Retrofit con base de tienda y generamos servicio
    }

    // Fábrica para UploadService (usa Authorization). No necesita cambios.
    fun createUploadService(context: Context): UploadService {
        val tokenManager = TokenManager(context) // Obtenemos el token desde TokenManager
        val client = baseOkHttpBuilder() // Builder base
            .addInterceptor(AuthInterceptor { tokenManager.getToken() }) // Interceptor de Authorization
            .build() // Construimos cliente
        return retrofit(storeBaseUrl, client).create(UploadService::class.java) // Reutilizamos storeBaseUrl para subida de archivos
    }

    fun createCartService(context: Context): CartService {
        val tokenManager = TokenManager(context)
        val client = baseOkHttpBuilder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
        return retrofit(storeBaseUrl, client).create(CartService::class.java)
    }
}