package com.example.snapproject.viewholder

import com.example.snapproject.databinding.ItemDetailSummaryBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailSummaryViewObject

class SummaryViewHoler(
    private val binding: ItemDetailSummaryBinding
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailSummaryViewObject
        binding.tvSummary.text = viewObject.summary
    }
}
