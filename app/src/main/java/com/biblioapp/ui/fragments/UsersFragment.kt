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
import androidx.appcompat.widget.SearchView
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentUsersAdminBinding
import com.biblioapp.model.User
import com.biblioapp.ui.adapter.UsersAdminAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersAdminBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersAdminAdapter
    private val TAG = "UsersFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate using ViewBinding and return binding.root (NO java.io.File or other types)
        _binding = FragmentUsersAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UsersAdminAdapter(
            onEdit = { user -> openEditUser(user) },
            onDelete = { user -> confirmDelete(user) },
            onToggleBlock = { user, blocked -> confirmToggleBlock(user, blocked) },
            onClick = { user -> /* abrir detalle si quieres */ }
        )

        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter

        binding.searchUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { filter(query); return true }
            override fun onQueryTextChange(newText: String?): Boolean { filter(newText); return true }
        })

        try { binding.swipeRefresh.setOnRefreshListener { loadUsers() } } catch (_: Exception) {}
        loadUsers()
    }

    private fun filter(query: String?) {
        val q = query?.trim()?.lowercase()
        if (q.isNullOrEmpty()) { loadUsers(); return }
        val current = adapter.currentList
        val filtered = current.filter {
            val nameMatch = (it.name ?: "").lowercase().contains(q) || (it.lastname ?: "").lowercase().contains(q)
            val emailMatch = (it.email ?: "").lowercase().contains(q)
            val phoneMatch = (it.phone ?: "").lowercase().contains(q)
            nameMatch || emailMatch || phoneMatch
        }
        adapter.submitList(filtered)
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            try {
                try { binding.swipeRefresh.isRefreshing = true } catch (_: Exception) {}
                val service = RetrofitClient.createUserService(requireContext())
                val list = withContext(Dispatchers.IO) { service.getUsers() }
                adapter.submitList(list)
                binding.tvUsersEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando usuarios: ${e.message}", e)
                Toast.makeText(requireContext(), "Error cargando usuarios: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                try { binding.swipeRefresh.isRefreshing = false } catch (_: Exception) {}
            }
        }
    }

    private fun confirmDelete(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Eliminar ${user.email ?: user.name}?")
            .setPositiveButton("Sí") { _, _ -> deleteUser(user) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteUser(user: User) {
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createUserService(requireContext())
                withContext(Dispatchers.IO) { service.deleteUser(user.id) }
                Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
                loadUsers()
            } catch (e: Exception) {
                handleApiException(e, "eliminar usuario")
            }
        }
    }

    private fun confirmToggleBlock(user: User, block: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle(if (block) "Bloquear usuario" else "Desbloquear usuario")
            .setMessage("${if (block) "Bloquear" else "Desbloquear"} a ${user.email ?: user.name}?")
            .setPositiveButton("Sí") { _, _ -> toggleBlock(user, block) }
            .setNegativeButton("No") { _, _ -> loadUsers() }
            .show()
    }

    private fun toggleBlock(user: User, block: Boolean) {
        lifecycleScope.launch {
            try {
                val service = RetrofitClient.createUserService(requireContext())
                val newStatus = if (block) "BLOQUEADO" else "ACTIVO"
                withContext(Dispatchers.IO) { service.updateUser(user.id, mapOf("status" to newStatus)) }
                Toast.makeText(requireContext(), "Usuario actualizado", Toast.LENGTH_SHORT).show()
                loadUsers()
            } catch (e: Exception) {
                handleApiException(e, "actualizar usuario")
            }
        }
    }

    private fun openEditUser(user: User) {
        Toast.makeText(requireContext(), "Editar ${user.email}", Toast.LENGTH_SHORT).show()
        // Aquí puedes abrir una Activity o Fragment para editar el usuario
    }

    private fun handleApiException(e: Exception, action: String) {
        if (e is HttpException) {
            val resp = e.response()
            val errBody = try { resp?.errorBody()?.string() } catch (ex: Exception) { null }
            Log.e(TAG, "HTTP ${e.code()} al $action: ${e.message()}, body=$errBody", e)
            Toast.makeText(requireContext(), "Error servidor: ${errBody ?: "HTTP ${e.code()}"}", Toast.LENGTH_LONG).show()
        } else {
            Log.e(TAG, "Error al $action: ${e.message}", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}