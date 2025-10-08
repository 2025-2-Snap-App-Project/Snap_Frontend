package com.example.snapproject.viewholder

import com.example.snapproject.applyStyleBetweenAsterisks
import com.example.snapproject.databinding.ItemDetailSummaryBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailSummaryViewObject

class SummaryViewHolder(
    private val binding: ItemDetailSummaryBinding,
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailSummaryViewObject
        binding.tvSummary.text = viewObject.summary.replace(" ", "\u00A0") // Word Wrap 제거 후 TextView 연결

        applyStyleBetweenAsterisks(binding.tvSummary, itemView.context) // ** 사이 단어에 텍스트 스타일 적용
    }
}
