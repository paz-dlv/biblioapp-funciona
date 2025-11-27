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

    // Nuevo: user id en memoria
    var currentUserId: Int? = null
        private set

    init {
        // Al iniciar, cargamos token, role y userId desde las preferencias
        currentToken = prefs.getString(KEY_TOKEN, null)
        currentRole = prefs.getString(KEY_USER_ROLE, null)
        val uid = prefs.getInt(KEY_USER_ID, -1)
        currentUserId = if (uid != -1) uid else null
    }

    /**
     * Guarda token + userName + userEmail (compatible con tu método actual).
     * No cambié la firma original para mantener retrocompatibilidad.
     */
    fun saveAuth(token: String, userName: String, userEmail: String, role: String? = null, userId: Int? = null) {
        currentToken = token
        if (!role.isNullOrBlank()) currentRole = normalizeRole(role)
        if (userId != null) currentUserId = userId

        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_USER_ROLE, currentRole)
            if (userId != null) putInt(KEY_USER_ID, userId) else remove(KEY_USER_ID)
            apply()
        }
    }

    // Método nuevo y pequeño para guardar solo el user id (mínimo cambio)
    fun saveUserId(userId: Int) {
        currentUserId = userId
        prefs.edit().putInt(KEY_USER_ID, userId).apply()

    }

    // Restauré este método que se llamaba desde MainActivity
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

    // Nuevo: devuelve user id si existe
    fun getUserId(): Int? = currentUserId ?: prefs.getInt(KEY_USER_ID, -1).let { if (it != -1) it else null }

    fun isLoggedIn(): Boolean = getToken() != null

    // Checks de role (normalizando mayúsculas/minúsculas)
    fun isAdmin(): Boolean = getRole()?.equals("ADMIN", ignoreCase = true) == true
    fun isClient(): Boolean = getRole()?.equals("CLIENT", ignoreCase = true) == true

    fun clear() {
        currentToken = null
        currentRole = null
        currentUserId = null
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
        private const val KEY_USER_ID = "user_id" // nuevo
    }
}