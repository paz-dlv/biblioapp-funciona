package com.biblioapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.biblioapp.R
import com.biblioapp.api.RetrofitClient
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.FragmentAdminDashboardBinding
import com.biblioapp.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    // Helper para evitar NullPointer si binding no contiene el campo generado
    private fun <T : View> findView(id: Int): T? = binding.root.findViewById(id)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tm = TokenManager(requireContext())

        val name = tm.getUserName().takeIf { !it.isNullOrBlank() } ?: "Administrador"
        val role = tm.getRole().takeIf { !it.isNullOrBlank() } ?: "ADMIN"

        // Header
        binding.tvHeaderWelcome.text = "Bienvenido,"
        binding.tvHeaderName.text = name
        binding.tvHeaderRole.text = "Rol: $role"
        binding.ivHeaderLogo.setImageResource(R.drawable.logo_app)
        binding.ivProfileAvatar.setImageResource(R.drawable.ic_user_avatar)

        // Referencia segura a TextViews (fallback a findViewById)
        val tvProducts = findView<TextView>(R.id.tvStatsProductsValue)
        val tvOrders = findView<TextView>(R.id.tvStatsOrdersValue)
        val tvUsers = findView<TextView>(R.id.tvStatsUsersValue)

        // Inicializar placeholders
        tvProducts?.text = "0"
        tvOrders?.text = "0"
        tvUsers?.text = "0"

        binding.btnLogoutProfile.setOnClickListener {
            tm.clear()
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        // CARGAR Y ACTUALIZAR LOS NÚMEROS DEL PANEL (productos, pedidos, usuarios)
        lifecycleScope.launch {
            try {
                val productService = RetrofitClient.createProductService(requireContext())
                val orderService = RetrofitClient.createOrderService(requireContext())
                val userService = RetrofitClient.createUserService(requireContext())

                val prodsDeferred = async(Dispatchers.IO) { productService.getProducts() }
                val ordersDeferred = async(Dispatchers.IO) { orderService.getOrders() }
                val usersDeferred = async(Dispatchers.IO) { userService.getUsers() }

                val prods = prodsDeferred.await()
                val orders = ordersDeferred.await()
                val users = usersDeferred.await()

                // Actualizar UI en hilo principal
                tvProducts?.text = prods.size.toString()
                tvOrders?.text = orders.size.toString()
                tvUsers?.text = users.size.toString()
            } catch (e: Exception) {
                tvProducts?.text = "0"
                tvOrders?.text = "0"
                tvUsers?.text = "0"
                Toast.makeText(requireContext(), "No se pudieron cargar los contadores: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}