package com.biblioapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // <- Asegurate de NO tener caracteres extra (p. ej. "-" antes de binding)
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tm = TokenManager(requireContext())

        // Datos del administrador desde TokenManager (ajusta si tus métodos tienen otros nombres)
        val name = tm.getUserName().takeIf { !it.isNullOrBlank() } ?: "Administrador"
        val email = tm.getUserEmail().takeIf { !it.isNullOrBlank() } ?: "sin email"
        val role = tm.getRole().takeIf { !it.isNullOrBlank() } ?: "ADMIN"

        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
        binding.tvProfileRole.text = role

        // Avatar: usar el drawable ic_user_avatar directamente (no generar iniciales)
        binding.ivProfileAvatar.setImageResource(R.drawable.ic_user_avatar)

        // Estadísticas placeholders (ajustadas a los ids del layout)
        binding.tvStatsProductsValue.text = "0"
        binding.tvStatsOrdersValue.text = "0"
        binding.tvStatsUsersValue.text = "0"

        // Botón Cerrar sesión -> limpia TokenManager y vuelve a MainActivity limpiando back stack
        binding.btnLogoutProfile.setOnClickListener {
            tm.clear()
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        // CARGAR Y ACTUALIZAR LOS NÚMEROS DEL PANEL (productos, pedidos, usuarios)
        loadCounts()
    }

    private fun loadCounts() {
        lifecycleScope.launch {
            try {
                val productService = RetrofitClient.createProductService(requireContext())
                val orderService = RetrofitClient.createOrderService(requireContext())
                val userService = RetrofitClient.createUserService(requireContext())

                // Ejecutar en paralelo para no bloquear el hilo UI
                val prodsDeferred = async(Dispatchers.IO) { productService.getProducts() }
                val ordersDeferred = async(Dispatchers.IO) { orderService.getOrders() }
                val usersDeferred = async(Dispatchers.IO) { userService.getUsers() }

                val prods = prodsDeferred.await()
                val orders = ordersDeferred.await()
                val users = usersDeferred.await()

                // Actualizar la UI con los conteos reales
                binding.tvStatsProductsValue.text = prods.size.toString()
                binding.tvStatsOrdersValue.text = orders.size.toString()
                binding.tvStatsUsersValue.text = users.size.toString()
            } catch (e: Exception) {
                // En caso de fallo dejamos 0 y mostramos un toast breve (opcional)
                binding.tvStatsProductsValue.text = "0"
                binding.tvStatsOrdersValue.text = "0"
                binding.tvStatsUsersValue.text = "0"
                Toast.makeText(requireContext(), "No se pudieron cargar los contadores: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}