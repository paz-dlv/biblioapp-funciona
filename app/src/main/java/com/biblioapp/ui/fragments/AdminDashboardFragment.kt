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
        // En tu layout vimos tvStatsProductsValue / tvStatsOrdersValue / tvStatsUsersValue
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}