package com.biblioapp.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentManageProductsBinding
import com.biblioapp.model.Product
import com.biblioapp.ui.ProductDetailActivity
import com.biblioapp.ui.adapter.ProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageProductsFragment : Fragment() {

    private var _binding: FragmentManageProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ProductAdapter

    private val TAG = "ManageProductsFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(
            onItemClick = { product ->
                // Abrir detalle/editar
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                intent.putExtra("product_id", product.id)
                startActivity(intent)
            },
            onAddClick = { product ->
                // Por si quieres un botón "Añadir" dentro del item, ejemplo: añadir al carrito etc.
                Toast.makeText(requireContext(), "Añadir ${product.title} (no implementado)", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.adapter = adapter

        binding.btnRefresh.setOnClickListener { loadProducts() }
        binding.btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Filtro no implementado todavía", Toast.LENGTH_SHORT).show()
        }

        // Swipe to delete (left or right)
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val product = adapter.getProductAt(pos)
                    if (product != null) confirmAndDelete(product, pos) else adapter.notifyItemChanged(pos)
                }
            }
        }
        ItemTouchHelper(simpleCallback).attachToRecyclerView(binding.recyclerProducts)

        // Cargar lista por primera vez
        loadProducts()
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            binding.btnRefresh.isEnabled = false
            binding.tvEmpty.text = "Cargando productos..."
            try {
                val service = RetrofitClient.createProductService(requireContext())
                val list = withContext(Dispatchers.IO) { service.getProducts() } // tu ProductService ya define getProducts()
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerProducts.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando productos: ${e.message}", e)
                Toast.makeText(requireContext(), "Error cargando productos", Toast.LENGTH_SHORT).show()
                binding.tvEmpty.text = "Error cargando productos"
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerProducts.visibility = View.GONE
            } finally {
                binding.btnRefresh.isEnabled = true
            }
        }
    }

    private fun confirmAndDelete(product: Product, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Eliminar '${product.title}'?")
            .setPositiveButton("Sí") { _, _ ->
                deleteProduct(product, position)
            }
            .setNegativeButton("No") { _, _ ->
                adapter.notifyItemChanged(position) // deshacer swipe
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun deleteProduct(product: Product, position: Int) {
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createProductService(requireContext())
                withContext(Dispatchers.IO) {
                    service.deleteProduct(product.id)
                }
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                // Recargar la lista (más fiable) o remover del adapter (optimización)
                loadProducts()
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando producto: ${e.message}", e)
                Toast.makeText(requireContext(), "No se pudo eliminar el producto", Toast.LENGTH_SHORT).show()
                adapter.notifyItemChanged(position)
            }
        }
    }

    fun onAddProductClicked() {
        // Abrir ProductDetailActivity para crear nuevo producto
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}