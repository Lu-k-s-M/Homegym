package com.example.homegym.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.homegym.data.model.HistorialEjercicio
import com.example.homegym.databinding.ItemHistorialBinding

class HistorialAdapter(
    private var items: List<HistorialEjercicio>,
    private val onDeleteClick: (HistorialEjercicio) -> Unit,
    private val onSelectionChanged: () -> Unit,
    private val onItemClick: (HistorialEjercicio) -> Unit
) : RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    val selectedIds = mutableSetOf<Int>()

    inner class ViewHolder(val binding: ItemHistorialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvNombre.text = item.ejercicioNombre
            tvFecha.text = item.fecha
            
            Glide.with(ivEjercicio.context)
                .load(item.ejercicioImagen)
                .centerCrop()
                .into(ivEjercicio)

            checkBox.isChecked = selectedIds.contains(item.id)
            
            root.setOnClickListener {
                onItemClick(item)
            }

            checkBox.setOnClickListener {
                toggleSelection(item.id)
            }

            btnDelete.setOnClickListener {
                item.id?.let { onDeleteClick(item) }
            }
        }
    }

    private fun toggleSelection(id: Int?) {
        if (id == null) return
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
        notifyDataSetChanged()
        onSelectionChanged()
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<HistorialEjercicio>) {
        items = newItems
        notifyDataSetChanged()
    }
}
