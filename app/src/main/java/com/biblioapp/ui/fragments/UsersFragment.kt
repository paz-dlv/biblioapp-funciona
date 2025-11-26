package com.biblioapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biblioapp.databinding.FragmentUsersBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: fetch users from API and show in RecyclerView
        CoroutineScope(Dispatchers.IO).launch {
            // val userService = RetrofitClient.createUserService(requireContext())
            // val users = userService.getUsers()
            // update UI on main thread...
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}