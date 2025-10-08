package com.example.snapproject.viewholder

import com.example.snapproject.databinding.ItemDetailStorageBinding
import com.example.snapproject.applyStyleBetweenAsterisks
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailStorageViewObject

class StorageViewHoler (
    private val binding: ItemDetailStorageBinding
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailStorageViewObject
        binding.tvStorage.text = viewObject.storage

        applyStyleBetweenAsterisks(binding.tvStorage, itemView.context) // ** 사이 단어에 텍스트 스타일 적용
    }
}
