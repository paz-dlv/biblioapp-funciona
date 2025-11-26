package com.biblioapp.util

object Role {
    // Valores tal como vienen desde Xano (mayúsculas en tu ejemplo)
    const val ADMIN = "ADMIN"
    const val CLIENT = "CLIENT"

    // Normalización a minúsculas si en algún sitio prefieres comparar así
    fun normalize(raw: String?): String? = raw?.trim()?.let { if (it.equals(ADMIN, ignoreCase = true)) ADMIN else if (it.equals(CLIENT, ignoreCase = true)) CLIENT else it }
}