package com.biblioapp.ui.fragments

import android.app.AlertDialog
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
import com.biblioapp.ui.AddProductActivity
import com.biblioapp.ui.adapter.AdminProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageProductsFragment : Fragment() {

    private var _binding: FragmentManageProductsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdminProductAdapter

    private val TAG = "ManageProductsFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // refrescar al volver de AddProductActivity/edición
        loadProducts()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminProductAdapter(
            onClick = { product ->
                // Abrir edición (AddProductActivity en modo editar)
                AddProductActivity.start(requireContext(), product.id)
            },
            onEdit = { product ->
                // Editar producto
                AddProductActivity.start(requireContext(), product.id)
            },
            onDelete = { product ->
                // Confirmación y borrado
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar producto")
                    .setMessage("¿Eliminar '${product.title}'?")
                    .setPositiveButton("Sí") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                val service = RetrofitClient.createProductService(requireContext())
                                withContext(Dispatchers.IO) { service.deleteProduct(product.id) }
                                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                                loadProducts()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error eliminando producto: ${e.message}", e)
                                Toast.makeText(requireContext(), "No se pudo eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("No", null)
                    .show()
            },
            onToggleActive = { product, isActive ->
                // Confirmar cambio de estado y aplicar PATCH
                AlertDialog.Builder(requireContext())
                    .setTitle(if (isActive) "Habilitar producto" else "Deshabilitar producto")
                    .setMessage(if (isActive) "¿Habilitar '${product.title}'?" else "¿Deshabilitar '${product.title}'?")
                    .setPositiveButton("Sí") { _, _ ->
                        lifecycleScope.launch {
                            try {
                                val service = RetrofitClient.createProductService(requireContext())
                                val body = mapOf("active" to isActive)
                                withContext(Dispatchers.IO) { service.updateProduct(product.id, body) }
                                Toast.makeText(requireContext(), "Estado actualizado", Toast.LENGTH_SHORT).show()
                                loadProducts()
                            } catch (e: Exception) {
                                Log.e(TAG, "Error actualizando estado: ${e.message}", e)
                                Toast.makeText(requireContext(), "No se pudo actualizar estado: ${e.message}", Toast.LENGTH_LONG).show()
                                loadProducts() // forzar refresh para restaurar switch visual
                            }
                        }
                    }
                    .setNegativeButton("No") { _, _ -> loadProducts() } // restaurar visual si cancela
                    .show()
            }
        )

        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.adapter = adapter

        binding.btnRefresh.setOnClickListener { loadProducts() }
        binding.btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Filtro no implementado todavía", Toast.LENGTH_SHORT).show()
        }

        // Si tienes un botón en el layout con id btnAddProduct lo conectamos para abrir AddProductActivity
        binding.root.findViewById<View>(resources.getIdentifier("btnAddProduct", "id", requireContext().packageName))?.setOnClickListener {
            onAddProductClicked()
        }

        // Swipe to delete (left or right) — todavía útil en admin
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val product = adapter.getProductAt(pos)
                    if (product != null) {
                        // reutilizar confirm dialog
                        AlertDialog.Builder(requireContext())
                            .setTitle("Eliminar producto")
                            .setMessage("¿Eliminar '${product.title}'?")
                            .setPositiveButton("Sí") { _, _ ->
                                lifecycleScope.launch {
                                    try {
                                        val service = RetrofitClient.createProductService(requireContext())
                                        withContext(Dispatchers.IO) { service.deleteProduct(product.id) }
                                        Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                                        loadProducts()
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error eliminando producto: ${e.message}", e)
                                        Toast.makeText(requireContext(), "No se pudo eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                                        adapter.notifyItemChanged(pos)
                                    }
                                }
                            }
                            .setNegativeButton("No") { _, _ -> adapter.notifyItemChanged(pos) }
                            .setOnCancelListener { adapter.notifyItemChanged(pos) }
                            .show()
                    } else adapter.notifyItemChanged(pos)
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
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerProducts.visibility = View.GONE

            try {
                val service = RetrofitClient.createProductService(requireContext())
                val list = withContext(Dispatchers.IO) { service.getProducts() }
                Log.d(TAG, "Productos cargados: ${list.size}")
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerProducts.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                if (list.isEmpty()) binding.tvEmpty.text = "No se encontraron productos"
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

    fun onAddProductClicked() {
        // Abrir AddProductActivity para crear nuevo producto
        AddProductActivity.start(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}