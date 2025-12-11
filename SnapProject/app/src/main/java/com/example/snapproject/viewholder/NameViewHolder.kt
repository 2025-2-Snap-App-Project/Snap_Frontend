package com.example.snapproject.viewholder

import com.example.snapproject.applyStyleBetweenAsterisks
import com.example.snapproject.databinding.ItemDetailNameBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailNameViewObject

class NameViewHolder(
    private val binding: ItemDetailNameBinding,
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailNameViewObject
        binding.tvName.text = viewObject.name.replace(" ", "\u00A0") // Word Wrap 제거 후 TextView 연결

        applyStyleBetweenAsterisks(binding.tvName, itemView.context) // ** 사이 단어에 텍스트 스타일 적용
    }

    // 아이템의 TextView 내용 return
    override fun getTextForTTS(): String {
        return binding.tvName.text.toString()
    }
}
