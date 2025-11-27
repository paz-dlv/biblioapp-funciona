package com.biblioapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.biblioapp.databinding.ItemProductBinding
import com.biblioapp.model.Product
import com.biblioapp.ui.ProductDetailActivity

class ProductAdapter(
    private var items: List<Product> = emptyList(),
    // parámetro nuevo opcional para manejar "Añadir" sin romper llamadas existentes
    private val onAddClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    // ViewHolder interno con referencia al ViewBinding del item
    class VH(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProductBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val product = items[position]

        // 1. Carga de imagen principal del producto (si existe)
        if (!product.image.isNullOrEmpty()) {
            val imageUrl = product.image[0].url // Asume que ProductImage tiene 'url'
            holder.binding.imgProduct.load(imageUrl) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }
        } else {
            holder.binding.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // 2. Clic en el producto: abre detalle
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_EXTRA", product)
            context.startActivity(intent)
        }

        // 3. Asignación de textos
        holder.binding.tvTitle.text = product.title
        holder.binding.tvAuthor?.text = product.author
        holder.binding.tvGenre?.text = product.genre
        holder.binding.tvDescription.text = product.description ?: ""
        holder.binding.tvPrice.text = "Precio: S/ ${product.price}"
        holder.binding.tvStock?.text = "Stock: ${product.stock}"

        // 4. Conectar el botón Añadir (si existe en el binding) al callback
        try {
            holder.binding.btnAdd.setOnClickListener {
                onAddClick(product)
            }
        } catch (e: Exception) {
            // Si por alguna razón btnAdd no existe en binding (error de binding), no hacemos nada.
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Actualiza la lista de productos y refresca la vista
     */
    fun updateData(newItems: List<Product>) {
        items = newItems
        notifyDataSetChanged()
    }
}