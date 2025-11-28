package com.biblioapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biblioapp.databinding.ItemUserAdminBinding
import com.biblioapp.model.User

class UsersAdminAdapter(
    private val onEdit: (User) -> Unit = {},
    private val onDelete: (User) -> Unit = {},
    // <-- aquí: usar { _, _ -> } como valor por defecto (dos parámetros)
    private val onToggleBlock: (User, Boolean) -> Unit = { _, _ -> },
    private val onClick: (User) -> Unit = {}
) : ListAdapter<User, UsersAdminAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(a: User, b: User) = a.id == b.id
            override fun areContentsTheSame(a: User, b: User) = a == b
        }
    }

    inner class VH(private val b: ItemUserAdminBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(u: User) {
            val fullName = listOfNotNull(u.name?.trim(), u.lastname?.trim()).filter { it.isNotEmpty() }.joinToString(" ")
            b.tvUserName.text = if (fullName.isNotEmpty()) fullName else (u.email ?: "Usuario #${u.id}")
            b.tvUserEmail.text = u.email ?: ""
            b.tvUserRole.text = u.role ?: ""

            val s = (u.status ?: "").uppercase()
            val isBlocked = s.contains("BLOCK") || s.contains("BLOQUE") || s.contains("BLOQUEADO")
            b.switchBlocked.setOnCheckedChangeListener(null)
            b.switchBlocked.isChecked = isBlocked
            b.switchBlocked.setOnCheckedChangeListener { _, checked ->
                onToggleBlock(u, checked)
            }

            b.btnUserEdit.setOnClickListener { onEdit(u) }
            b.btnUserDelete.setOnClickListener { onDelete(u) }
            b.root.setOnClickListener { onClick(u) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemUserAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}