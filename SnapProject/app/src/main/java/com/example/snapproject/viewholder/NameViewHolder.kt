package com.example.snapproject.viewholder

import com.example.snapproject.databinding.ItemDetailNameBinding
import com.example.snapproject.applyStyleBetweenAsterisks
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailNameViewObject

class NameViewHolder(
    private val binding: ItemDetailNameBinding
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailNameViewObject
        binding.tvName.text = viewObject.name

        applyStyleBetweenAsterisks(binding.tvName, itemView.context) // ** 사이 단어에 텍스트 스타일 적용
    }
}
