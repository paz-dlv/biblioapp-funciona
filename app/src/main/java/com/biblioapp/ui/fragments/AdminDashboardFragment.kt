package com.biblioapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.FragmentAdminDashboardBinding
import com.biblioapp.ui.MainActivity

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

        // Mostrar datos del administrador (si existen)
        binding.tvProfileName.text = tm.getUserName().takeIf { !it.isNullOrBlank() } ?: "Administrador"
        binding.tvProfileEmail.text = tm.getUserEmail().takeIf { !it.isNullOrBlank() } ?: "sin email"
        binding.tvProfileRole.text = tm.getRole().takeIf { !it.isNullOrBlank() } ?: "ADMIN"

        // Estadísticas (placeholder — sustituir por datos reales)
        binding.tvStatsProducts.text = "0"
        binding.tvStatsOrders.text = "0"
        binding.tvStatsUsers.text = "0"

        // Logout button: clear token and navigate to MainActivity
        binding.btnLogoutProfile.setOnClickListener {
            tm.clear()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}