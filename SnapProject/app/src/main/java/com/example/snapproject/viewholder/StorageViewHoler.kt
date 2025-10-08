package com.example.snapproject.viewholder

import com.example.snapproject.databinding.ItemDetailStorageBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailStorageViewObject

class StorageViewHoler (
    private val binding: ItemDetailStorageBinding
) : DetailViewHolder(binding) {
    override fun bind(item: DetailItemData) {
        val viewObject = item.detailViewObject as DetailStorageViewObject
        binding.tvStorage.text = viewObject.storage
    }
}
