package com.example.homegym.ui.rutinas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homegym.data.model.Rutina
import com.example.homegym.databinding.ItemRutinaBinding

class RutinaAdapter(
    private var rutinas: List<Rutina>,
    private val onDeleteClick: (Rutina) -> Unit,
    private val onSelectionChanged: () -> Unit,
    private val onItemClick: (Rutina) -> Unit
) : RecyclerView.Adapter<RutinaAdapter.RutinaViewHolder>() {

    val selectedIds = mutableSetOf<Int>()

    class RutinaViewHolder(val binding: ItemRutinaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RutinaViewHolder {
        val binding = ItemRutinaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RutinaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RutinaViewHolder, position: Int) {
        val rutina = rutinas[position]
        holder.binding.apply {
            tvNombre.text = rutina.nombre
            tvDescripcion.text = rutina.descripcion ?: "Sin descripción"
            tvEjerciciosCount.text = "${rutina.ejercicios.size} ejercicios"
            
            tvDuracion.text = "${rutina.duracionMinutos} min"
            tvCalorias.text = "${rutina.calorias} kcal"

            checkBox.isChecked = selectedIds.contains(rutina.id)

            contentLayout.setOnClickListener {
                onItemClick(rutina)
            }

            checkBox.setOnClickListener {
                rutina.id?.let { toggleSelection(it) }
            }

            btnDelete.setOnClickListener {
                onDeleteClick(rutina)
            }
        }
    }

    private fun toggleSelection(id: Int) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    override fun getItemCount() = rutinas.size

    fun updateData(newRutinas: List<Rutina>) {
        rutinas = newRutinas
        notifyDataSetChanged()
    }
}
