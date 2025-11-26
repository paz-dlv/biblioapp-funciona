package com.biblioapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tm = TokenManager(requireContext())
        binding.tvWelcome.text = "Panel de administración"
        binding.tvInfo.text = "Usuario: ${tm.getUserEmail() ?: "—"}\nRol: ${tm.getRole() ?: "—"}"

        // TODO: carga estadísticas reales desde API y actualiza tvStatsProducts/tvStatsOrders/tvStatsUsers
        binding.tvStatsProducts.text = "Productos: 0"
        binding.tvStatsOrders.text = "Pedidos: 0"
        binding.tvStatsUsers.text = "Usuarios: 0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}