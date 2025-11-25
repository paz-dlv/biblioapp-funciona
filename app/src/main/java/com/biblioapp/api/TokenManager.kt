package com.biblioapp.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) { // Clase que encapsula el acceso a SharedPreferences
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ¡¡CAMBIO AQUÍ!!
    // Esta variable contendrá el token en memoria durante el proceso de login.
    var currentToken: String? = null
        private set // La hacemos de solo lectura desde fuera

    init {
        // Al iniciar, cargamos el token desde las preferencias
        currentToken = prefs.getString(KEY_TOKEN, null)
    }

    fun saveAuth(token: String, userName: String, userEmail: String) {
        currentToken = token // Actualizamos la variable en memoria
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            apply()
        }
    }

    // El interceptor usará este métodoo, que ahora devolverá el token en memoria si existe.
    fun getToken(): String? {
        return currentToken
    }

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun isLoggedIn(): Boolean = getToken() != null

    fun clear() {
        currentToken = null // Limpiamos la variable en memoria
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }
}