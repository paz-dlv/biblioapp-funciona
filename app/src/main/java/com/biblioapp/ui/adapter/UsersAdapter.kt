package com.biblioapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biblioapp.databinding.ItemUserBinding
import com.biblioapp.model.User

class UsersAdapter(private val onClick: (User) -> Unit = {}) :
    ListAdapter<User, UsersAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
        }
    }

    inner class VH(private val b: ItemUserBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(u: User) {
            // nombre preferido: name + lastname si existe
            val displayName = listOfNotNull(u.name, u.lastname).filter { it.isNotBlank() }.joinToString(" ").ifBlank { u.email ?: "Usuario" }
            b.tvUserName.text = displayName
            b.tvUserEmail.text = u.email ?: "-"
            b.tvUserRole.text = u.role ?: "-"
            b.root.setOnClickListener { onClick(u) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}