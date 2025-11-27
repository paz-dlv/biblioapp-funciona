package com.biblioapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView setup (id: rvCartItems)
        adapter = CartAdapter(items, this)
        binding.rvCartItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCartItems.adapter = adapter

        // Initial UI state
        binding.tvEmpty.visibility = View.VISIBLE
        binding.tvEmpty.text = "Cargando..."
        binding.rvCartItems.visibility = View.GONE
        binding.btnCheckout.visibility = View.GONE

        loadCartAndItems()

        binding.btnCheckout.setOnClickListener {
            Toast.makeText(requireContext(), "Finalizar compra (implementa checkout)", Toast.LENGTH_SHORT).show()
        }
    }

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
     * Fetch cart_items and populate Product if available:
     * - If API returned product directly (product), use it.
     * - Else if API returned _product (array), use the first element.
     * - Else try to fetch product by id via ProductService (if available).
     */
    private fun fetchAndShowItems(cartId: Int?) {
        lifecycleScope.launch {
            try {
                val cartService = RetrofitClient.createCartService(requireContext())
                val rawItems = withContext(Dispatchers.IO) { cartService.getCartItems(cartId) }
                Log.d(TAG, "GET /cart_item?cart_id=$cartId returned ${rawItems.size} items: $rawItems")

                if (rawItems.isEmpty()) {
                    adapter.updateList(emptyList())
                    showEmpty(true)
                    binding.btnCheckout.visibility = View.GONE
                    return@launch
                }

                // First pass: use product or _product if present
                val initialMerge: List<CartItem> = rawItems.map { ci ->
                    val embedded = ci.product ?: ci._product?.firstOrNull()
                    if (embedded != null) ci.copy(product = embedded) else ci
                }

                // Find which product_ids still missing product data
                val missingIds = initialMerge.filter { it.product == null }.map { it.product_id }.distinct()
                val finalList: List<CartItem> = if (missingIds.isEmpty()) {
                    initialMerge
                } else {
                    // Try to fetch products by id (if ProductService exists)
                    val productService: ProductService? = try {
                        RetrofitClient.createProductService(requireContext())
                    } catch (e: Exception) {
                        Log.w(TAG, "createProductService not available: ${e.message}")
                        null
                    }

                    if (productService == null) {
                        // fallback: keep initialMerge (will show placeholders)
                        initialMerge
                    } else {
                        // fetch products concurrently
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
                        // Merge into final list
                        initialMerge.map { ci ->
                            if (ci.product != null) ci else {
                                val p = productsMap[ci.product_id]
                                if (p != null) ci.copy(product = p) else ci
                            }
                        }
                    }
                }

                // Update adapter with final list
                adapter.updateList(finalList)

                // Debug logs
                Log.d(TAG, "AFTER updateList -> adapter.itemCount = ${adapter.itemCount}")
                binding.rvCartItems.post {
                    Log.d(TAG, "RV state: visibility=${binding.rvCartItems.visibility} height=${binding.rvCartItems.height} childCount=${binding.rvCartItems.childCount}")
                }

                showEmpty(finalList.isEmpty())
                binding.btnCheckout.visibility = if (finalList.isNotEmpty()) View.VISIBLE else View.GONE

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

    override fun onQuantityChanged(item: CartItem, newQty: Int) {
        Log.d(TAG, "onQuantityChanged item=${item.id} newQty=$newQty")
    }

    override fun onRemove(item: CartItem) {
        Log.d(TAG, "onRemove item=${item.id}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}