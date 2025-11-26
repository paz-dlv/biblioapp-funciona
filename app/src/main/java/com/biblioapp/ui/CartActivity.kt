package com.biblioapp.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.ActivityCartBinding
import com.biblioapp.model.Cart
import com.biblioapp.model.CartItem
import com.biblioapp.model.CreateCartItemRequest
import com.biblioapp.model.UpdateCartItemRequest
import com.biblioapp.ui.adapter.CartAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CartActivity : AppCompatActivity(), CartAdapter.Listener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter
    private val items = mutableListOf<CartItem>()
    private var currentCart: Cart? = null

    private val TAX_RATE = 0.05
    private val SHIPPING = 4.99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = CartAdapter(items, this)
        binding.rvCart.adapter = adapter

        setupSwipeToDelete()

        binding.btnApply.setOnClickListener { applyCoupon() }
        binding.btnCheckout.setOnClickListener { checkout() }

        loadCartAndItems()
    }

    private fun loadCartAndItems() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createCartService(this@CartActivity)
                // Primero intentamos obtener carts (si el backend asocia carts al usuario)
                val carts = try {
                    service.getCarts(null)
                } catch (e: Exception) {
                    emptyList<Cart>()
                }
                if (carts.isNotEmpty()) {
                    currentCart = carts.first()
                    loadItemsForCart(currentCart!!.id)
                } else {
                    // fallback: intentar obtener items sin cartId
                    loadItemsForCart(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // fallback a obtener items sin cart id
                loadItemsForCart(null)
            }
        }
    }

    private suspend fun fetchItemsFromService(cartId: Int?): List<CartItem> {
        return withContext(Dispatchers.IO) {
            val service = RetrofitClient.createCartService(this@CartActivity)
            service.getCartItems(cartId)
        }
    }

    private fun loadItemsForCart(cartId: Int?) {
        lifecycleScope.launch {
            try {
                val list = fetchItemsFromService(cartId)
                items.clear()
                items.addAll(list)
                adapter.updateList(items)
                showEmpty(items.isEmpty())
                recalcTotals()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CartActivity, "Error cargando carrito", Toast.LENGTH_SHORT).show()
                showEmpty(true)
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.rvCart.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmpty(empty: Boolean) {
        // Accedemos a la vista raíz del binding incluido
        binding.viewEmpty.root.visibility = if (empty) View.VISIBLE else View.GONE
        binding.rvCart.visibility = if (empty) View.GONE else View.VISIBLE
        binding.summaryContainer.visibility = if (empty) View.GONE else View.VISIBLE
    }

    private fun recalcTotals() {
        val subtotal = items.sumOf { (it.product?.price ?: 0.0) * it.quantity }
        val tax = subtotal * TAX_RATE
        val shipping = if (subtotal > 0) SHIPPING else 0.0
        val total = subtotal + tax + shipping

        binding.tvSubtotal.text = String.format("$%.2f", subtotal)
        binding.tvTax.text = String.format("$%.2f", tax)
        binding.tvShipping.text = String.format("$%.2f", shipping)
        binding.tvTotal.text = String.format("$%.2f", total)
    }

    override fun onQuantityChanged(item: CartItem, newQty: Int) {
        // Actualizar en backend
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createCartService(this@CartActivity)
                val updated = withContext(Dispatchers.IO) {
                    service.updateCartItem(item.id, UpdateCartItemRequest(quantity = newQty))
                }
                // actualizar localmente
                val idx = items.indexOfFirst { it.id == item.id }
                if (idx >= 0) {
                    items[idx] = updated
                    adapter.updateList(items)
                }
                recalcTotals()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CartActivity, "No se pudo actualizar la cantidad", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRemove(item: CartItem) {
        // Confirmar y llamar delete
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Eliminar '${item.product?.title ?: "este ítem"}' del carrito?")
            .setPositiveButton("Sí") { _, _ ->
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val service = RetrofitClient.createCartService(this@CartActivity)
                            service.deleteCartItem(item.id)
                        }
                        val idx = items.indexOfFirst { it.id == item.id }
                        if (idx >= 0) {
                            items.removeAt(idx)
                            adapter.notifyItemRemoved(idx)
                        }
                        recalcTotals()
                        if (items.isEmpty()) showEmpty(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@CartActivity, "No se pudo eliminar el ítem", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setupSwipeToDelete() {
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = items[pos]
                    // revert swipe until user confirms
                    // mostramos dialog y si cancela restablecemos
                    AlertDialog.Builder(this@CartActivity)
                        .setTitle("Eliminar")
                        .setMessage("¿Eliminar '${item.product?.title ?: "este ítem"}' del carrito?")
                        .setPositiveButton("Sí") { _, _ ->
                            lifecycleScope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        val service = RetrofitClient.createCartService(this@CartActivity)
                                        service.deleteCartItem(item.id)
                                    }
                                    items.removeAt(pos)
                                    adapter.notifyItemRemoved(pos)
                                    recalcTotals()
                                    if (items.isEmpty()) showEmpty(true)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(this@CartActivity, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                    adapter.notifyItemChanged(pos)
                                }
                            }
                        }
                        .setNegativeButton("No") { d, _ ->
                            d.dismiss()
                            adapter.notifyItemChanged(pos)
                        }
                        .show()
                }
            }
        }
        ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.rvCart)
    }

    // Coupon simple: DISCOUNT10 => 10% off
    private fun applyCoupon() {
        val code = binding.etCoupon.text.toString().trim()
        if (code.isEmpty()) {
            Toast.makeText(this, "Ingresa un código", Toast.LENGTH_SHORT).show()
            return
        }
        val subtotal = items.sumOf { (it.product?.price ?: 0.0) * it.quantity }
        if (subtotal <= 0) {
            Toast.makeText(this, "Carrito vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (code.equals("DISCOUNT10", ignoreCase = true)) {
            val discount = subtotal * 0.10
            val tax = (subtotal - discount) * TAX_RATE
            val shipping = if (subtotal - discount > 0) SHIPPING else 0.0
            val total = subtotal - discount + tax + shipping

            binding.tvSubtotal.text = String.format("$%.2f", subtotal - discount)
            binding.tvTax.text = String.format("$%.2f", tax)
            binding.tvShipping.text = String.format("$%.2f", shipping)
            binding.tvTotal.text = String.format("$%.2f", total)

            Toast.makeText(this, "Cupón aplicado: -${String.format("$%.2f", discount)}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Cupón inválido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkout() {
        if (items.isEmpty()) {
            Toast.makeText(this, "Carrito vacío", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Confirmar pago")
            .setMessage("¿Deseas simular el pago y finalizar la compra?")
            .setPositiveButton("Pagar") { _, _ ->
                lifecycleScope.launch {
                    binding.btnCheckout.isEnabled = false
                    try {
                        // Payload simple usando ids y qty
                        val payloadItems = items.map { mapOf("id" to it.id, "quantity" to it.quantity) }
                        withContext(Dispatchers.IO) {
                            // Si quieres hacer una llamada real, usa createCart/createCartItem/checkout endpoint según tu API.
                            // Aquí hacemos una simulación local: podrías llamar a un endpoint si existe.
                        }
                        Toast.makeText(this@CartActivity, "Compra realizada (simulada). Gracias.", Toast.LENGTH_LONG).show()
                        items.clear()
                        adapter.updateList(items)
                        showEmpty(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@CartActivity, "Error en el checkout", Toast.LENGTH_SHORT).show()
                    } finally {
                        binding.btnCheckout.isEnabled = true
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}