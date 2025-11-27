package com.biblioapp.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.biblioapp.api.RetrofitClient
import com.biblioapp.model.CreateCartItemRequest
import com.biblioapp.model.CreateCartRequest
import com.biblioapp.model.Product
import com.biblioapp.api.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CartHelper {
    private const val TAG = "CartHelper"

    /**
     * Añade un producto al carrito de forma segura:
     * - Si existe un cart del usuario: usarlo
     * - Si no, validar si el cart guardado (prefs) pertenece al usuario; si no, eliminarlo
     * - Si no hay ninguno válido: crear uno nuevo y guardarlo
     */
    suspend fun addProductToCart(context: Context, product: Product, qty: Int = 1): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val service = RetrofitClient.createCartService(context)
                val tokenManager = TokenManager(context)
                val currentUserId = tokenManager.getUserId()

                Log.d(TAG, "Current user id from TokenManager = $currentUserId")

                // Obtener lista de carts del backend
                val carts = try {
                    service.getCarts(null)
                } catch (e: Exception) {
                    Log.w(TAG, "getCarts failed: ${e.message}")
                    emptyList()
                }
                Log.d(TAG, "getCarts returned ${carts.size} entries: $carts")

                // 1) Buscar cart que pertenezca al usuario actual
                val userCart = if (currentUserId != null) {
                    carts.firstOrNull { it.user_id == currentUserId }
                } else null

                // 2) Revisar saved cart y validarlo si existe
                val savedId = CartManager.getCartId(context)
                var useSavedId: Int? = null
                if (savedId != null) {
                    // Si getCarts devolvió datos, intentamos validar savedId contra la lista
                    if (carts.isNotEmpty()) {
                        val savedCartObject = carts.firstOrNull { it.id == savedId }
                        if (savedCartObject != null) {
                            // Si el saved cart pertenece al mismo usuario o user desconocido (currentUserId == null), permitimos usarlo.
                            if (currentUserId == null || savedCartObject.user_id == currentUserId) {
                                useSavedId = savedId
                                Log.d(TAG, "Saved cart id $savedId validated against backend list and will be used")
                            } else {
                                // saved cart pertenece a otro usuario -> lo borramos de prefs para evitar reutilizarlo
                                Log.w(TAG, "Saved cart id $savedId belongs to different user (saved user=${savedCartObject.user_id} vs current=$currentUserId). Clearing saved cart id.")
                                CartManager.clearCartId(context)
                            }
                        } else {
                            // No pudimos encontrar savedId en la lista (posible inconsistencia). Lo borramos para seguridad.
                            Log.w(TAG, "Saved cart id $savedId not found in backend carts list. Clearing saved cart id to avoid using stale cart.")
                            CartManager.clearCartId(context)
                        }
                    } else {
                        // carts list vacío; no podemos validar. Podemos optar por usar savedId (asumiendo que es válido)
                        useSavedId = savedId
                        Log.d(TAG, "No carts returned by backend; using saved cart id $savedId (cannot validate against backend list).")
                    }
                }

                // 3) Decidir cartId final: prioridad userCart -> validated savedId -> crear nuevo
                val cartId = when {
                    userCart != null -> {
                        Log.d(TAG, "Using cart that belongs to current user: id=${userCart.id}")
                        CartManager.saveCartId(context, userCart.id)
                        userCart.id
                    }
                    useSavedId != null -> {
                        Log.d(TAG, "Using previously saved cart id=$useSavedId")
                        useSavedId
                    }
                    else -> {
                        if (currentUserId == null) {
                            Log.w(TAG, "No user identified and no valid saved cart; creating anonymous cart.")
                        } else {
                            Log.d(TAG, "No valid cart found for user $currentUserId; creating new cart.")
                        }
                        val uid = currentUserId ?: 0
                        val created = service.createCart(CreateCartRequest(user_id = uid))
                        Log.d(TAG, "Created cart: $created")
                        CartManager.saveCartId(context, created.id)
                        created.id
                    }
                }

                // 4) Crear cart_item en el cartId seleccionado
                val req = CreateCartItemRequest(cart_id = cartId, product_id = product.id, quantity = qty)
                Log.d(TAG, "Creating cart_item with payload: $req")
                val createdItem = service.createCartItem(req)
                Log.d(TAG, "createCartItem response: $createdItem")

                // Guardar cart_id devuelto por createCartItem por si acaso
                try { createdItem.cart_id.let { CartManager.saveCartId(context, it) } } catch (_: Exception) {}

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Añadido al carrito", Toast.LENGTH_SHORT).show()
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "addProductToCart error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No se pudo añadir al carrito", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
    }
}