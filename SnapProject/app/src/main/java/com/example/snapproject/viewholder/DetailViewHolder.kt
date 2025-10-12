package com.example.snapproject.viewholder

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.snapproject.model.DetailItemData

abstract class DetailViewHolder(
    binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {
    abstract fun bind(item: DetailItemData)

    // ViewType별 ViewHolder에서 TTS용 텍스트를 반환하는 추상 함수
    abstract fun getTextForTTS(): String
}
