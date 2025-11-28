package com.biblioapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biblioapp.databinding.ItemOrderAdminBinding
import com.biblioapp.model.Order
import com.biblioapp.utils.CurrencyUtils

/**
 * Adapter para el panel admin de órdenes.
 * Reemplaza el OrdersAdapter en la vista admin (no sobrescribe el adapter cliente).
 *
 * Callbacks:
 *  - onAccept(order)
 *  - onReject(order)
 *  - onSend(order)
 */
class OrdersAdminAdapter(
    private val onAccept: (Order) -> Unit = {},
    private val onReject: (Order) -> Unit = {},
    private val onSend: (Order) -> Unit = {}
) : ListAdapter<Order, OrdersAdminAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Order, newItem: Order) = oldItem == newItem
        }
    }

    inner class VH(private val b: ItemOrderAdminBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(o: Order) {
            b.tvOrderTitle.text = "Pedido #${o.id}"

            val displayTotal = o.total?.let {
                // Ajusta según cómo tu API entrega el total (unidad o centavos)
                String.format("$%.2f", it)
            } ?: "$0.00"

            b.tvOrderInfo.text = "Total: $displayTotal • Estado: ${o.estado ?: "PENDIENTE"}"

            val estado = (o.estado ?: "").uppercase()
            val isAccepted = estado.contains("ACEPT") || estado.contains("ACCEPT")
            // Habilitar botón enviar solo si está aceptado
            b.btnSend.isEnabled = isAccepted

            // Click listeners
            b.btnAccept.setOnClickListener { onAccept(o) }
            b.btnReject.setOnClickListener { onReject(o) }
            b.btnSend.setOnClickListener { onSend(o) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}