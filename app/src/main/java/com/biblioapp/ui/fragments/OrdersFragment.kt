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
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentOrdersBinding
import com.biblioapp.model.Order
import com.biblioapp.ui.adapter.OrdersAdminAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrdersAdminAdapter
    private val TAG = "OrdersFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrdersAdminAdapter(
            onAccept = { order -> confirmChangeStatus(order, "ACEPTADA") },
            onReject = { order -> confirmChangeStatus(order, "RECHAZADA") },
            onSend = { order -> confirmSend(order) }
        )

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        try { binding.swipeRefresh.setOnRefreshListener { loadOrders() } } catch (_: Exception) {}
        loadOrders()
    }

    private fun loadOrders() {
        lifecycleScope.launch {
            try {
                try { binding.swipeRefresh.isRefreshing = true } catch (_: Exception) {}
                val service = RetrofitClient.createOrderService(requireContext())
                val list: List<Order> = withContext(Dispatchers.IO) { service.getOrders() }
                adapter.submitList(list)
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando órdenes: ${e.message}", e)
                Toast.makeText(requireContext(), "Error cargando órdenes: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                try { binding.swipeRefresh.isRefreshing = false } catch (_: Exception) {}
            }
        }
    }

    private fun confirmChangeStatus(order: Order, newStatus: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(if (newStatus == "ACEPTADA") "Aceptar pedido" else "Rechazar pedido")
            .setMessage("¿Deseas ${if (newStatus == "ACEPTADA") "aceptar" else "rechazar"} el pedido #${order.id}?")
            .setPositiveButton("Sí") { _, _ -> changeStatus(order, newStatus) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun changeStatus(order: Order, newStatus: String) {
        lifecycleScope.launch {
            try {
                binding.swipeRefresh.isEnabled = false
                val service = RetrofitClient.createOrderService(requireContext())
                // Enviar 'estado' en lugar de 'status'
                withContext(Dispatchers.IO) {
                    service.updateOrder(order.id, mapOf("estado" to newStatus))
                }
                Toast.makeText(requireContext(), "Pedido #${order.id} actualizado a $newStatus", Toast.LENGTH_SHORT).show()
                loadOrders()
            } catch (e: Exception) {
                handleApiException(e, "actualizando pedido")
            } finally {
                try { binding.swipeRefresh.isEnabled = true } catch (_: Exception) {}
            }
        }
    }

    private fun confirmSend(order: Order) {
        AlertDialog.Builder(requireContext())
            .setTitle("Enviar pedido")
            .setMessage("¿Marcar y enviar el pedido #${order.id}?")
            .setPositiveButton("Sí") { _, _ -> sendOrder(order) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun sendOrder(order: Order) {
        lifecycleScope.launch {
            try {
                // Validación local: solo permitir enviar si está ACEPTADA
                val estadoActual = (order.estado ?: order.estado ?: "").uppercase()
                val isAccepted = estadoActual.contains("ACEPT") || estadoActual.contains("ACCEPT")
                if (!isAccepted) {
                    Toast.makeText(requireContext(), "Sólo se puede enviar un pedido que esté ACEPTADO", Toast.LENGTH_LONG).show()
                    return@launch
                }

                binding.swipeRefresh.isEnabled = false
                val service = RetrofitClient.createOrderService(requireContext())
                withContext(Dispatchers.IO) {
                    // Actualizamos 'estado' a ENVIADA
                    service.updateOrder(order.id, mapOf("estado" to "ENVIADA"))
                }
                Toast.makeText(requireContext(), "Pedido #${order.id} marcado como ENVIADO", Toast.LENGTH_SHORT).show()
                loadOrders()
            } catch (e: Exception) {
                handleApiException(e, "enviando pedido")
            } finally {
                try { binding.swipeRefresh.isEnabled = true } catch (_: Exception) {}
            }
        }
    }

    private fun handleApiException(e: Exception, action: String) {
        if (e is HttpException) {
            val resp = e.response()
            val errBody = try { resp?.errorBody()?.string() } catch (ex: Exception) { null }
            Log.e(TAG, "HTTP ${e.code()} al $action: ${e.message()}, body=$errBody", e)
            val userMsg = when {
                !errBody.isNullOrBlank() -> "Error servidor: ${extractMessageFromErrorBody(errBody) ?: errBody}"
                else -> "Error servidor: HTTP ${e.code()}"
            }
            Toast.makeText(requireContext(), userMsg, Toast.LENGTH_LONG).show()
        } else {
            Log.e(TAG, "Error no-HTTP al $action: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al $action: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun extractMessageFromErrorBody(body: String): String? {
        return try {
            val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex(RegexOption.IGNORE_CASE)
            regex.find(body)?.groups?.get(1)?.value
        } catch (ex: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}