package com.biblioapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioapp.api.RetrofitClient
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.FragmentManageProductsBinding
import com.biblioapp.ui.ProductDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageProductsFragment : Fragment() {

    private var _binding: FragmentManageProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManageProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView placeholder
        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        // binding.recyclerProducts.adapter = productAdapter (implementa tu adapter)

        // Example: load products (ajusta endpoint si lo necesitas)
        val tm = TokenManager(requireContext())
        val ctx = requireContext()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val productService = RetrofitClient.createProductService(ctx)
                // val products = productService.getProducts()
                // withContext(Dispatchers.Main) { productAdapter.submitList(products) }
            } catch (t: Throwable) {
                // manejar error (log o mostrar toast)
            }
        }

        binding.btnRefresh.setOnClickListener {
            // refrescar lista
        }
    }

    fun onAddProductClicked() {
        // Abrir ProductDetailActivity para crear nuevo producto (ajusta según tu flujo)
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}