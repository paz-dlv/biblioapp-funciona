package com.biblioapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.biblioapp.databinding.ItemProductBinding
import com.biblioapp.model.Product
import com.biblioapp.ui.ProductDetailActivity

/**
 * ProductAdapter modernizado:
 * - Usa ListAdapter + DiffUtil para actualizaciones más eficientes y animadas.
 * - Expone onItemClick y onAddClick callbacks.
 * - Mantiene compatibilidad con el flujo que abre ProductDetailActivity.
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
            // Imagen
            val imageUrl = product.image?.firstOrNull()?.url
            if (!imageUrl.isNullOrBlank()) {
                binding.imgProduct.load(imageUrl) {
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_dialog_alert)
                }
            } else {
                binding.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Textos
            binding.tvTitle.text = product.title
            binding.tvAuthor?.text = product.author ?: ""
            binding.tvGenre?.text = product.genre ?: ""
            binding.tvDescription?.text = product.description ?: ""
            // Formatea price como número con 2 decimales
            binding.tvPrice.text = String.format("$%.2f", product.price ?: 0.0)
            binding.tvStock?.text = "Stock: ${product.stock ?: 0}"

            // Click sobre el item
            binding.root.setOnClickListener {
                onItemClick(product)
                // Compatibilidad con comportamiento anterior: abrir ProductDetailActivity si se desea
                val ctx = binding.root.context
                val intent = Intent(ctx, ProductDetailActivity::class.java)
                // Intent original usaba pasar el objeto; si Product no es Parcelable/Serializable
                // puedes pasar product.id en vez del objeto completo.
                intent.putExtra("product_id", product.id)
                ctx.startActivity(intent)
            }

            // Botón añadir (si existe en layout)
            try {
                binding.btnAdd.setOnClickListener {
                    onAddClick(product)
                }
            } catch (_: Exception) {
                // si el binding no contiene btnAdd, ignoramos
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

    // Helper público si necesitas acceder al item por posición desde la Activity/Fragment
    fun getProductAt(position: Int): Product? = runCatching { getItem(position) }.getOrNull()
}