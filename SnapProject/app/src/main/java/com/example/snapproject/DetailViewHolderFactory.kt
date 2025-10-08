package com.example.snapproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.snapproject.databinding.ItemDetailDateBinding
import com.example.snapproject.databinding.ItemDetailNameBinding
import com.example.snapproject.databinding.ItemDetailStorageBinding
import com.example.snapproject.databinding.ItemDetailSummaryBinding
import com.example.snapproject.viewholder.DateViewHolder
import com.example.snapproject.viewholder.DetailViewHolder
import com.example.snapproject.viewholder.NameViewHolder
import com.example.snapproject.viewholder.StorageViewHolder
import com.example.snapproject.viewholder.SummaryViewHolder

object DetailViewHolderFactory {
    fun createViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder{
        return when(viewType) {
            ViewType.DETAIL_NAME.ordinal -> NameViewHolder(getViewBinding(parent, ItemDetailNameBinding::inflate))
            ViewType.DETAIL_DATE.ordinal -> DateViewHolder(getViewBinding(parent, ItemDetailDateBinding::inflate))
            ViewType.DETAIL_STORAGE.ordinal -> StorageViewHolder(getViewBinding(parent, ItemDetailStorageBinding::inflate))
            ViewType.DETAIL_SUMMARY.ordinal -> SummaryViewHolder(getViewBinding(parent, ItemDetailSummaryBinding::inflate))
            else -> SummaryViewHolder(getViewBinding(parent, ItemDetailSummaryBinding::inflate))
        }
    }

    private fun <T : ViewBinding> getViewBinding(
        parent: ViewGroup,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> T
    ): T {
        return inflate(LayoutInflater.from(parent.context), parent, false)
    }

}
