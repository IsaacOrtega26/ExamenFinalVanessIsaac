package com.example.fieldlog.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fieldlog.data.Record
import com.example.fieldlog.databinding.ItemRecordBinding
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter(
    private val onItemClick: (Record) -> Unit,
    private val onDeleteClick: (Record) -> Unit
) : ListAdapter<Record, RecordAdapter.RecordViewHolder>(RecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record)
    }

    inner class RecordViewHolder(private val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: Record) {
            binding.tvTitle.text = record.title
            binding.tvDescription.text = record.description

            val date = Date(record.date)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(date)

            binding.tvLocation.text = "Lat: ${String.format("%.4f", record.latitude)}, Lng: ${String.format("%.4f", record.longitude)}"

            binding.btnDelete.setOnClickListener {
                onDeleteClick(record)
            }

            binding.root.setOnClickListener {
                onItemClick(record)
            }
        }
    }
}

class RecordDiffCallback : DiffUtil.ItemCallback<Record>() {
    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem == newItem
    }
}

