package com.biblioapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.biblioapp.databinding.ItemProductAdminBinding
import com.biblioapp.model.Product
import com.biblioapp.utils.CurrencyUtils

/**
 * Adapter específico para la vista Admin.
 * - Infla item_product_admin.xml (ItemProductAdminBinding)
 * - Callbacks: onEdit, onDelete, onToggleActive, onClick (opcional)
 */
class AdminProductAdapter(
    private val onClick: (Product) -> Unit = {},
    private val onEdit: (Product) -> Unit = {},
    private val onDelete: (Product) -> Unit = {},
    private val onToggleActive: (Product, Boolean) -> Unit = { _, _ -> }
) : ListAdapter<Product, AdminProductAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
            override fun areContentsTheSame(a: Product, b: Product) = a == b
        }
    }

    inner class VH(private val b: ItemProductAdminBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(p: Product) {
            // Imagen
            val imageUrl = p.image?.firstOrNull()?.url
            if (!imageUrl.isNullOrBlank()) {
                b.imgAdminProduct.load(imageUrl) {
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_dialog_alert)
                }
            } else {
                b.imgAdminProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Textos
            b.tvAdminTitle.text = p.title ?: "-"
            b.tvAdminAuthor.text = p.author ?: ""
            b.tvAdminGenre.text = p.genre ?: ""
            b.tvAdminDescription.text = p.description ?: ""
            b.tvAdminPrice.text = CurrencyUtils.formatClp(p.price)
            b.tvAdminStock.text = "Stock: ${p.stock ?: 0}"


            // Evitar que el listener se dispare al hacer bind
            b.switchAdminActive.setOnCheckedChangeListener(null)

            b.switchAdminActive.setOnCheckedChangeListener { _, isChecked ->
                onToggleActive(p, isChecked)
            }

            // Clicks
            b.root.setOnClickListener { onClick(p) }
            b.btnAdminEdit.setOnClickListener { onEdit(p) }
            b.btnAdminDelete.setOnClickListener { onDelete(p) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    // utilidad para swipe/delete desde fragment
    fun getProductAt(position: Int): Product? = runCatching { getItem(position) }.getOrNull()
}