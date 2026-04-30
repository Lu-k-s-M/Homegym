package com.example.homegym.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.homegym.data.model.HistorialEjercicio
import com.example.homegym.databinding.ItemEjercicioHorizontalBinding

class HistorialHorizontalAdapter(private var items: List<HistorialEjercicio>) :
    RecyclerView.Adapter<HistorialHorizontalAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemEjercicioHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEjercicioHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvNombre.text = item.ejercicioNombre
            tvIntensidad.text = "" // El historial no suele guardar la intensidad, o podríamos añadirla si el modelo la tuviera
            tvIntensidad.visibility = View.GONE
            tvCalorias.text = item.fecha // Mostramos la fecha en lugar de calorías para diferenciar
            
            Glide.with(ivThumbnail.context)
                .load(item.ejercicioImagen)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(ivThumbnail)

            root.setOnClickListener {
                val intent = Intent(it.context, PlaybackActivity::class.java).apply {
                    putExtra("EJERCICIO_ID", item.ejercicioId)
                    putExtra("VIDEO_TITLE", item.ejercicioNombre)
                    // La URL del vídeo no está en el modelo de historial reducido, 
                    // PlaybackActivity debería poder cargarla por ID o le pasamos una de reserva.
                    // En PlaybackActivity ya implementamos una de reserva si no llega.
                }
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<HistorialEjercicio>) {
        items = newItems
        notifyDataSetChanged()
    }
}
