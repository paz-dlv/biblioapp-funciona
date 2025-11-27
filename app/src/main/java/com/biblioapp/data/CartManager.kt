package com.biblioapp.data

import android.content.Context

object CartManager {
    private const val PREFS = "cart_prefs"
    private const val KEY_CART_ID = "cart_id"

    fun saveCartId(context: Context, cartId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CART_ID, cartId).apply()
    }

    fun getCartId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_CART_ID, -1)
        return if (id != -1) id else null
    }

    fun clearCartId(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CART_ID).apply()
    }
}