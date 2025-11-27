package com.biblioapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.biblioapp.databinding.ItemCartBinding
import com.biblioapp.model.CartItem
import com.biblioapp.api.ApiConfig
import com.bumptech.glide.Glide

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val listener: Listener
) : RecyclerView.Adapter<CartAdapter.VH>() {

    interface Listener {
        fun onQuantityChanged(item: CartItem, newQty: Int)
        fun onRemove(item: CartItem)
    }

    inner class VH(private val b: ItemCartBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: CartItem) {
            b.tvTitle.text = item.product?.title ?: "Sin título"
            b.tvAuthor.text = item.product?.author ?: ""
            b.tvPrice.text = String.format("$%.2f", item.product?.price ?: 0.0)
            b.tvQty.text = item.quantity.toString()
            b.tvStock.text = "En stock: ${item.product?.stock ?: 0}"

            // Obtener ProductImage de forma segura
            val firstImage = item.product?.image?.firstOrNull()
            val imageUrl = when {
                !firstImage?.url.isNullOrBlank() -> firstImage?.url
                !firstImage?.path.isNullOrBlank() -> {
                    val path = firstImage!!.path
                    if (path.startsWith("http")) path else ApiConfig.storeBaseUrl.trimEnd('/') + "/" + path.trimStart('/')
                }
                else -> null
            }

            Glide.with(b.root)
                .load(imageUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(b.imgCover)

            b.btnPlus.setOnClickListener {
                val wanted = item.quantity + 1
                val stock = item.product?.stock ?: Int.MAX_VALUE
                if (wanted > stock) {
                    b.root.context.toast("No hay stock suficiente (max $stock)")
                } else {
                    item.quantity = wanted
                    b.tvQty.text = item.quantity.toString()
                    listener.onQuantityChanged(item, item.quantity)
                }
            }
            b.btnMinus.setOnClickListener {
                val wanted = item.quantity - 1
                if (wanted <= 0) {
                    listener.onRemove(item)
                } else {
                    item.quantity = wanted
                    b.tvQty.text = item.quantity.toString()
                    listener.onQuantityChanged(item, item.quantity)
                }
            }
            b.btnDelete.setOnClickListener {
                listener.onRemove(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // FIX: copia defensiva para evitar el self-aliasing bug cuando newList es la misma instancia que 'items'
    fun updateList(newList: List<CartItem>) {
        val snapshot = ArrayList(newList) // copia de los datos de entrada
        items.clear()
        items.addAll(snapshot)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position in 0 until items.size) {
            val item = items.removeAt(position)
            notifyItemRemoved(position)
            listener.onRemove(item)
        }
    }
}

// extensión corta para Toast
private fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}