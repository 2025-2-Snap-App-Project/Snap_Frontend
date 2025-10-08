package com.example.snapproject.viewholder

import com.example.snapproject.databinding.ItemDetailSummaryBinding
import com.example.snapproject.applyStyleBetweenAsterisks
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailSummaryViewObject

class SummaryViewHoler(
    private val binding: ItemDetailSummaryBinding
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailSummaryViewObject
        binding.tvSummary.text = viewObject.summary

        applyStyleBetweenAsterisks(binding.tvSummary, itemView.context) // ** 사이 단어에 텍스트 스타일 적용
    }
}
