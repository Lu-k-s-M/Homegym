package com.example.homegym.ui.rutinas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homegym.data.model.Ejercicio
import com.example.homegym.databinding.ItemEjercicioBinding
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.homegym.R

class EjercicioSeleccionAdapter(
    private var todosLosEjercicios: List<Ejercicio>,
    private val onSelectionChanged: (List<Int>) -> Unit
) : RecyclerView.Adapter<EjercicioSeleccionAdapter.ViewHolder>() {

    private var ejerciciosFiltrados = todosLosEjercicios
    private val seleccionados = mutableSetOf<Int>()

    fun updateData(newEjercicios: List<Ejercicio>) {
        todosLosEjercicios = newEjercicios
        ejerciciosFiltrados = newEjercicios
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        ejerciciosFiltrados = if (query.isEmpty()) {
            todosLosEjercicios
        } else {
            todosLosEjercicios.filter { 
                it.nombre.contains(query, ignoreCase = true) || 
                it.descripcion.contains(query, ignoreCase = true) ||
                it.parteCuerpo.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemEjercicioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEjercicioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ejercicio = ejerciciosFiltrados[position]
        holder.binding.apply {
            tvNombre.text = ejercicio.nombre
            tvDescripcion.text = ejercicio.descripcion
            tvIntensidad.text = ejercicio.intensidad
            tvCalorias.text = "${ejercicio.calorias} kcal"

            val isSelected = seleccionados.contains(ejercicio.id)
            
            val context = root.context
            if (isSelected) {
                root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.hg_background_dark))
                tvNombre.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvDescripcion.setTextColor(Color.LTGRAY)
                tvIntensidad.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvCalorias.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.hg_background_light))
                tvNombre.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvDescripcion.setTextColor(Color.DKGRAY)
                tvIntensidad.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvCalorias.setTextColor(ContextCompat.getColor(context, R.color.black))
            }

            root.setOnClickListener {
                ejercicio.id?.let { id ->
                    if (seleccionados.contains(id)) {
                        seleccionados.remove(id)
                    } else {
                        seleccionados.add(id)
                    }
                    notifyItemChanged(position)
                    onSelectionChanged(seleccionados.toList())
                }
            }
        }
    }

    override fun getItemCount() = ejerciciosFiltrados.size
}