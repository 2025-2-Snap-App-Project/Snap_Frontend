package com.example.snapproject.viewholder

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.snapproject.model.DetailItemData

abstract class DetailViewHolder (
    binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item: DetailItemData)
}
