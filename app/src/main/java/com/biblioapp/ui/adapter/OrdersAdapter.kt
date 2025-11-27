package com.biblioapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biblioapp.databinding.ItemOrderBinding
import com.biblioapp.model.Order

class OrdersAdapter(private val onClick: (Order) -> Unit = {}) :
    ListAdapter<Order, OrdersAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
        }
    }

    inner class VH(private val b: ItemOrderBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(o: Order) {
            b.tvOrderId.text = "Pedido #${o.id}"
            // Ajusta el formateo del total según la unidad que uses.
            // Si 'total' viene en centavos, divide por 100.0 para mostrar con decimales.
            val displayTotal = o.total?.let {
                // cambio comentado: usar it/100.0 si total está en centavos
                String.format("$%.2f", it) // o String.format("$%.2f", it / 100.0)
            } ?: "$0.00"
            b.tvOrderInfo.text = "Total: $displayTotal • Estado: ${o.status ?: "?"}"
            b.root.setOnClickListener { onClick(o) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}