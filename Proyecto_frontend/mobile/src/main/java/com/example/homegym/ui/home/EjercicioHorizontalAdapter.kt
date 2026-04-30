package com.example.homegym.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.homegym.data.model.Ejercicio
import com.example.homegym.databinding.ItemEjercicioHorizontalBinding

class EjercicioHorizontalAdapter(private val ejercicios: List<Ejercicio>) :
    RecyclerView.Adapter<EjercicioHorizontalAdapter.ViewHolder>() {

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
        val ejercicio = ejercicios[position]
        holder.binding.apply {
            tvNombre.text = ejercicio.nombre
            tvIntensidad.text = ejercicio.intensidad
            tvCalorias.text = "${ejercicio.calorias} kcal"
            
            Glide.with(ivThumbnail.context)
                .load(ejercicio.imagenUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivThumbnail)

            root.setOnClickListener {
                val intent = Intent(it.context, PlaybackActivity::class.java).apply {
                    putExtra("VIDEO_URL", ejercicio.videoUrl ?: "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                    putExtra("VIDEO_TITLE", ejercicio.nombre)
                    putExtra("EJERCICIO_ID", ejercicio.id)
                }
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = ejercicios.size
}
