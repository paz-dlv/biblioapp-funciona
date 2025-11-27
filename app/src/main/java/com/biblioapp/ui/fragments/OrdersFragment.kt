package com.biblioapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentOrdersBinding
import com.biblioapp.ui.adapter.OrdersAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OrdersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrdersAdapter()
        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrders.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            binding.tvEmpty.text = "Cargando pedidos..."
            try {
                val service = RetrofitClient.createOrderService(requireContext())
                val list = withContext(Dispatchers.IO) { service.getOrders() } // asegúrate de que existe getOrders()
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerOrders.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error cargando pedidos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}