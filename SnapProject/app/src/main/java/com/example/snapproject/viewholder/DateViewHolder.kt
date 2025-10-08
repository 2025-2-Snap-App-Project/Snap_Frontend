package com.example.snapproject.viewholder

import com.example.snapproject.databinding.ItemDetailDateBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailDateViewObject

class DateViewHolder(
    private val binding: ItemDetailDateBinding
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailDateViewObject
        binding.tvDate.text = viewObject.date
    }
}
