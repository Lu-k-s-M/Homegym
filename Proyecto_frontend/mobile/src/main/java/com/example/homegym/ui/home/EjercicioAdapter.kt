package com.example.homegym.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homegym.data.model.Ejercicio
import com.example.homegym.databinding.ItemEjercicioBinding

class EjercicioAdapter(private val ejercicios: List<Ejercicio>) :
    RecyclerView.Adapter<EjercicioAdapter.EjercicioViewHolder>() {

    class EjercicioViewHolder(val binding: ItemEjercicioBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EjercicioViewHolder {
        val binding = ItemEjercicioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EjercicioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EjercicioViewHolder, position: Int) {
        val ejercicio = ejercicios[position]
        holder.binding.apply {
            tvNombre.text = ejercicio.nombre
            tvDescripcion.text = ejercicio.descripcion
            tvIntensidad.text = "Intensidad: ${ejercicio.intensidad}"
            tvCalorias.text = "${ejercicio.calorias} kcal"

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
