package com.biblioapp.ui.fragments

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
import com.biblioapp.databinding.FragmentUsersBinding
import com.biblioapp.model.User
import com.biblioapp.ui.adapter.UsersAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UsersAdapter

    private val TAG = "UsersFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UsersAdapter { user ->
            Toast.makeText(requireContext(), "${user.email}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUsers.adapter = adapter

        loadUsers()
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            binding.tvEmpty.text = "Cargando usuarios..."
            binding.tvEmpty.visibility = View.VISIBLE
            binding.recyclerUsers.visibility = View.GONE

            try {
                Log.d(TAG, "Llamando a createUserService()...")
                val service = RetrofitClient.createUserService(requireContext())

                val list: List<User> = withContext(Dispatchers.IO) { service.getUsers() }

                Log.d(TAG, "Usuarios cargados: ${list.size}")
                list.take(10).forEachIndexed { i, u ->
                    Log.d(TAG, "user[$i]: id=${u.id} email=${u.email} name=${u.name} role=${u.role}")
                }

                adapter.submitList(list)

                val empty = list.isEmpty()
                binding.tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
                binding.recyclerUsers.visibility = if (empty) View.GONE else View.VISIBLE
                if (empty) binding.tvEmpty.text = "No se encontraron usuarios"
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando usuarios: ${e.message}", e)
                binding.tvEmpty.text = "Error cargando usuarios: ${e.message ?: "unknown"}"
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerUsers.visibility = View.GONE
                Toast.makeText(requireContext(), "Error cargando usuarios: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}