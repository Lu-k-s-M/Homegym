package com.example.homegym.ui.rutinas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homegym.data.model.Rutina
import com.example.homegym.databinding.ItemRutinaBinding

class RutinaAdapter(private val rutinas: List<Rutina>) :
    RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder>() {

    class RutinaViewHolder(val binding: ItemRutinaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val binding = ItemRutinaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RutinaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        val rutina = rutinas[position]
        holder.binding.tvNombre.text = rutina.nombre
        holder.binding.tvDescripcion.text = rutina.descripcion ?: "Sin descripción"
        holder.binding.tvEjerciciosCount.text = "${rutina.ejercicios.size} ejercicios"
    }

    override fun getItemCount() = rutinas.size
}
