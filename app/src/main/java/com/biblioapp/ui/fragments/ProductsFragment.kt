package com.biblioapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentProductsBinding
import com.biblioapp.model.Product
import com.biblioapp.ui.ProductDetailActivity
import com.biblioapp.ui.adapter.ProductAdapter
import com.biblioapp.data.CartHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private var allProducts: List<Product> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupSearch()
        loadProducts()
    }

    private fun setupRecycler() {
        // Construimos el adapter usando la nueva firma (onItemClick, onAddClick)
        adapter = ProductAdapter(
            onItemClick = { product ->
                // Abrir detalle al click sobre el item
                val ctx = requireContext()
                val intent = Intent(ctx, ProductDetailActivity::class.java)
                intent.putExtra("product_id", product.id)
                ctx.startActivity(intent)
            },
            onAddClick = { product ->
                // Añadir al carrito desde el botón del item
                viewLifecycleOwner.lifecycleScope.launch {
                    Toast.makeText(requireContext(), "Añadiendo ${product.title}...", Toast.LENGTH_SHORT).show()
                    val ok = CartHelper.addProductToCart(requireContext(), product, 1)
                    if (ok) {
                        // opcional: actualizar badge en la activity si tienes esa API
                        // (activity as? MainActivity)?.updateCartBadge()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo añadir al carrito", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun filter(query: String?) {
        val q = query?.trim()?.lowercase().orEmpty()
        if (q.isBlank()) {
            // Usar submitList en lugar de updateData
            adapter.submitList(allProducts)
        } else {
            val filtered = allProducts.filter { it.title.lowercase().contains(q) }
            adapter.submitList(filtered)
        }
    }

    private fun loadProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val service = RetrofitClient.createProductService(requireContext())
                val products = withContext(Dispatchers.IO) {
                    service.getProducts()
                }
                allProducts = products
                // actualizamos la lista con submitList
                adapter.submitList(products)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error cargando productos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}