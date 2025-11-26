package com.biblioapp.api

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) { // Clase que encapsula el acceso a SharedPreferences
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Token y role en memoria durante la sesión (útiles para el interceptor y checks rápidos)
    var currentToken: String? = null
        private set

    var currentRole: String? = null
        private set

    init {
        // Al iniciar, cargamos token y role desde las preferencias
        currentToken = prefs.getString(KEY_TOKEN, null)
        currentRole = prefs.getString(KEY_USER_ROLE, null)
    }

    /**
     * Guarda token + userName + userEmail (compatible con tu método actual).
     * Además acepta role opcional para guardar directamente.
     */
    fun saveAuth(token: String, userName: String, userEmail: String, role: String? = null) {
        currentToken = token // Actualizamos la variable en memoria
        if (!role.isNullOrBlank()) {
            currentRole = normalizeRole(role)
        }
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_ROLE, currentRole)
            apply()
        }
    }

    // Método separado para guardar solo el role (útil si lo obtienes posteriormente)
    fun saveRole(role: String?) {
        currentRole = normalizeRole(role)
        prefs.edit().putString(KEY_USER_ROLE, currentRole).apply()
    }

    // El interceptor usará este método (devuelve el token en memoria si existe).
    fun getToken(): String? = currentToken

    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    // Role accessors
    fun getRole(): String? = currentRole ?: prefs.getString(KEY_USER_ROLE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    // Checks de role (normalizando mayúsculas/minúsculas)
    fun isAdmin(): Boolean = getRole()?.equals("ADMIN", ignoreCase = true) == true
    fun isClient(): Boolean = getRole()?.equals("CLIENT", ignoreCase = true) == true

    fun clear() {
        currentToken = null
        currentRole = null
        prefs.edit().clear().apply()
    }

    private fun normalizeRole(raw: String?): String? {
        return raw?.trim()?.let {
            when {
                it.equals("ADMIN", ignoreCase = true) -> "ADMIN"
                it.equals("CLIENT", ignoreCase = true) -> "CLIENT"
                else -> it.uppercase()
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
    }
}