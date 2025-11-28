package com.biblioapp.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.biblioapp.databinding.ItemImagePreviewBinding

/**
 * Adapter simple para previsualizar imágenes seleccionadas antes de subir.
 * Usa ItemImagePreviewBinding y la ImageView con id imgPreview en item_image_preview.xml.
 */
class ImagePreviewAdapter(private val items: List<Uri>) : RecyclerView.Adapter<ImagePreviewAdapter.VH>() {

    inner class VH(private val binding: ItemImagePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            // Asegúrate de que tu item_image_preview.xml tenga ImageView con id @+id/imgPreview
            binding.imgPreview.load(uri) {
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_dialog_alert)
            }
            // Si quieres añadir eliminar por item, añade un botón en el layout y un callback aquí.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemImagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}