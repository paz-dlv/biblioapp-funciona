package com.biblioapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.biblioapp.databinding.ItemProductBinding
import com.biblioapp.model.Product
import com.biblioapp.utils.CurrencyUtils

/**
 * ProductAdapter modernizado:
 * - Usa ListAdapter + DiffUtil para actualizaciones más eficientes y animadas.
 * - Expone onItemClick y onAddClick callbacks.
 * - No arranca Activities por su cuenta: delega la navegación al callback onItemClick.
 * - Mantiene getProductAt(pos) para operaciones como swipe-to-delete.
 */
class ProductAdapter(
    private val onItemClick: (Product) -> Unit = {},
    private val onAddClick: (Product) -> Unit = {}
) : ListAdapter<Product, ProductAdapter.VH>(Diff) {

    companion object {
        private val Diff = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean =
                oldItem == newItem
        }
    }

    inner class VH(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            // Imagen (primera disponible)
            val imageUrl = product.image?.firstOrNull()?.url
            if (!imageUrl.isNullOrBlank()) {
                binding.imgProduct.load(imageUrl) {
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_dialog_alert)
                }
            } else {
                binding.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Textos (seguro con fallback)
            binding.tvTitle.text = product.title ?: "-"
            binding.tvAuthor?.text = product.author ?: ""
            binding.tvGenre?.text = product.genre ?: ""
            binding.tvDescription?.text = product.description ?: ""

            // Formateo CLP (ej: 19990 -> $19.990)
            binding.tvPrice.text = CurrencyUtils.formatClp(product.price)

            binding.tvStock?.text = "Stock: ${product.stock ?: 0}"

            // Click: delegamos la navegación/acción al callback
            binding.root.setOnClickListener { onItemClick(product) }

            // Botón añadir (si existe en el layout)
            try {
                binding.btnAdd.setOnClickListener { onAddClick(product) }
            } catch (_: Exception) {
                // Si el layout no dispone de btnAdd, ignoramos silenciosamente
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Helper público para obtener el producto en una posición (útil para swipe/delete).
     */
    fun getProductAt(position: Int): Product? = runCatching { getItem(position) }.getOrNull()
}