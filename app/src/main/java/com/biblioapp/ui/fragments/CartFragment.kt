package com.biblioapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioapp.api.ProductService
import com.biblioapp.api.RetrofitClient
import com.biblioapp.api.TokenManager
import com.biblioapp.data.CartManager
import com.biblioapp.databinding.FragmentCartBinding
import com.biblioapp.model.Cart
import com.biblioapp.model.CartItem
import com.biblioapp.model.Product
import com.biblioapp.model.UpdateCartItemRequest
import com.biblioapp.ui.adapter.CartAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartFragment : Fragment(), CartAdapter.Listener {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val TAG = "CartFragment"
    private lateinit var adapter: CartAdapter
    private val items = mutableListOf<CartItem>()
    private var currentCart: Cart? = null

    // Totals config
    private val TAX_RATE = 0.05
    private val SHIPPING = 4.99

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CartAdapter(items, this)
        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter

        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvEmpty.text = "Cargando..."
        binding.rvCartItems.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE

        loadCartAndItems()

        // Hook checkout button to simulation
        binding.btnCheckout.setOnClickListener {
            simulateCheckout()
        }
    }

    // Carga inicial: decide cart guardado o buscar carts del backend
    private fun loadCartAndItems() {
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createCartService(requireContext())
                val tokenManager = TokenManager(requireContext())
                val currentUserId = tokenManager.getUserId()

                val savedId = CartManager.getCartId(requireContext())
                if (savedId != null) {
                    Log.d(TAG, "Using saved cart id (priority) = $savedId")
                    currentCart = null
                    fetchAndShowItems(savedId)
                    return@launch
                }

                val carts = try {
                    withContext(Dispatchers.IO) { service.getCarts(null) }
                } catch (e: Exception) {
                    Log.w(TAG, "getCarts failed: ${e.message}")
                    emptyList<Cart>()
                }
                Log.d(TAG, "getCarts returned ${carts.size} entries: $carts")

                val userCart = if (currentUserId != null) carts.firstOrNull { it.user_id == currentUserId } else null

                if (userCart != null) {
                    currentCart = userCart
                    CartManager.saveCartId(requireContext(), userCart.id)
                    fetchAndShowItems(userCart.id)
                } else {
                    fetchAndShowItems(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadCartAndItems error: ${e.message}", e)
                val savedFallback = CartManager.getCartId(requireContext())
                if (savedFallback != null) fetchAndShowItems(savedFallback) else showEmpty(true)
            }
        }
    }

    /**
     * Trae cart_items y asegura product en cada item:
     * - usa product si vino embebido
     * - usa _product.first si Xano devolvió _product
     * - si falta, intenta obtener products por id via ProductService
     */
    private fun fetchAndShowItems(cartId: Int?) {
        lifecycleScope.launch {
            try {
                val cartService = RetrofitClient.createCartService(requireContext())
                val rawItems = withContext(Dispatchers.IO) { cartService.getCartItems(cartId) }
                Log.d(TAG, "GET /cart_item?cart_id=$cartId returned ${rawItems.size} items")

                if (rawItems.isEmpty()) {
                    items.clear()
                    adapter.updateList(items)
                    showEmpty(true)
                    binding.btnCheckout.visibility = View.GONE
                    return@launch
                }

                val initialMerge: List<CartItem> = rawItems.map { ci ->
                    val embedded = ci.product ?: ci._product?.firstOrNull()
                    if (embedded != null) ci.copy(product = embedded) else ci
                }

                val missingIds = initialMerge.filter { it.product == null }.map { it.product_id }.distinct()
                val finalList: List<CartItem> = if (missingIds.isEmpty()) {
                    initialMerge
                } else {
                    val productService: ProductService? = try { RetrofitClient.createProductService(requireContext()) } catch (e: Exception) {
                        Log.w(TAG, "createProductService not available: ${e.message}")
                        null
                    }

                    if (productService == null) {
                        initialMerge
                    } else {
                        val productsMap = mutableMapOf<Int, Product?>()
                        withContext(Dispatchers.IO) {
                            missingIds.map { id ->
                                async {
                                    try {
                                        val p = productService.getProduct(id)
                                        productsMap[id] = p
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Failed to fetch product $id: ${e.message}")
                                        productsMap[id] = null
                                    }
                                }
                            }.awaitAll()
                        }
                        initialMerge.map { ci ->
                            if (ci.product != null) ci else {
                                val p = productsMap[ci.product_id]
                                if (p != null) ci.copy(product = p) else ci
                            }
                        }
                    }
                }

                items.clear()
                items.addAll(finalList)
                adapter.updateList(items)

                Log.d(TAG, "AFTER updateList -> adapter.itemCount = ${adapter.itemCount}")
                binding.rvCartItems.post {
                    Log.d(TAG, "RV state: visibility=${binding.rvCartItems.visibility} height=${binding.rvCartItems.height} childCount=${binding.rvCartItems.childCount}")
                }

                showEmpty(items.isEmpty())
                binding.btnCheckout.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "fetchAndShowItems error: ${e.message}", e)
                showEmpty(true)
            }
        }
    }

    private fun showEmpty(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvCartItems.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmpty.text = "Tu carrito está vacío"
            binding.btnCheckout.visibility = View.GONE
        } else {
            binding.rvCartItems.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
            binding.btnCheckout.visibility = View.VISIBLE
        }
    }

    /**
     * Actualiza cantidad en backend y preserva el objeto product embebido
     * (el servidor suele devolver solo id/quantity; por eso hacemos merge)
     */
    override fun onQuantityChanged(item: CartItem, newQty: Int) {
        Log.d(TAG, "onQuantityChanged item=${item.id} newQty=$newQty")
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createCartService(requireContext())
                val updated = withContext(Dispatchers.IO) {
                    service.updateCartItem(item.id, UpdateCartItemRequest(quantity = newQty))
                }

                val idx = items.indexOfFirst { it.id == updated.id }
                if (idx >= 0) {
                    val existing = items[idx]
                    val merged = when {
                        updated.product != null -> updated
                        existing.product != null -> updated.copy(product = existing.product, _product = existing._product)
                        existing._product != null -> updated.copy(product = existing._product.firstOrNull(), _product = existing._product)
                        else -> updated
                    }
                    items[idx] = merged
                    adapter.updateList(items)
                } else {
                    fetchAndShowItems(currentCart?.id ?: CartManager.getCartId(requireContext()))
                }
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo actualizar cantidad en backend: ${e.message}", e)
                Toast.makeText(requireContext(), "No se pudo actualizar la cantidad", Toast.LENGTH_SHORT).show()
                fetchAndShowItems(currentCart?.id ?: CartManager.getCartId(requireContext()))
            }
        }
    }

    override fun onRemove(item: CartItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar")
            .setMessage("¿Eliminar '${item.product?.title ?: "este ítem"}' del carrito?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val service = RetrofitClient.createCartService(requireContext())
                            service.deleteCartItem(item.id)
                        }
                        val idx = items.indexOfFirst { it.id == item.id }
                        if (idx >= 0) {
                            items.removeAt(idx)
                            adapter.updateList(items)
                        }
                        Toast.makeText(requireContext(), "Ítem eliminado", Toast.LENGTH_SHORT).show()
                        showEmpty(items.isEmpty())
                        binding.btnCheckout.visibility = if (items.isNotEmpty()) View.VISIBLE else View.GONE
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting item: ${e.message}", e)
                        Toast.makeText(requireContext(), "No se pudo eliminar el ítem", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    // --- Checkout simulation functions ---

    // small data holder since Kotlin stdlib doesn't have a Quadruple
    private data class Quadruple<A,B,C,D>(val a:A, val b:B, val c:C, val d:D)

    private fun calcTotals(): Quadruple<Double, Double, Double, Double> {
        val subtotal = items.sumOf { (it.product?.price ?: 0.0) * it.quantity }
        val tax = subtotal * TAX_RATE
        val shipping = if (subtotal > 0) SHIPPING else 0.0
        val total = subtotal + tax + shipping
        return Quadruple(subtotal, tax, shipping, total)
    }

    private fun simulateCheckout() {
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Carrito vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val (subtotal, tax, shipping, total) = calcTotals()

        val message = StringBuilder().apply {
            append("Subtotal: ${String.format("$%.2f", subtotal)}\n")
            append("Impuestos: ${String.format("$%.2f", tax)}\n")
            append("Envío: ${String.format("$%.2f", shipping)}\n\n")
            append("Total: ${String.format("$%.2f", total)}\n\n")
            append("¿Deseas simular el pago y vaciar el carrito?")
        }.toString()

        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar pago")
            .setMessage(message)
            .setPositiveButton("Pagar") { _, _ -> performSimulatedPayment() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performSimulatedPayment() {
        binding.btnCheckout.isEnabled = false
        Toast.makeText(requireContext(), "Procesando pago...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createCartService(requireContext())

                // Make a snapshot of IDs to avoid concurrent modification
                val idsToDelete = items.map { it.id }

                // Delete sequentially on IO dispatcher (reliable)
                withContext(Dispatchers.IO) {
                    idsToDelete.forEach { id ->
                        try {
                            service.deleteCartItem(id)
                        } catch (e: Exception) {
                            Log.w(TAG, "deleteCartItem($id) failed: ${e.message}")
                        }
                    }
                }

                // Clear local state
                items.clear()
                adapter.updateList(items)
                showEmpty(true)

                // Clear saved cart id if your CartManager supports it
                try {
                    CartManager.clearCartId(requireContext())
                } catch (e: Exception) {
                    Log.w(TAG, "CartManager.clearCartId not available or failed: ${e.message}")
                }

                Toast.makeText(requireContext(), "Compra simulada realizada. Gracias.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "performSimulatedPayment error: ${e.message}", e)
                Toast.makeText(requireContext(), "Error al procesar pago simulado", Toast.LENGTH_SHORT).show()
                fetchAndShowItems(currentCart?.id ?: CartManager.getCartId(requireContext()))
            } finally {
                binding.btnCheckout.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}