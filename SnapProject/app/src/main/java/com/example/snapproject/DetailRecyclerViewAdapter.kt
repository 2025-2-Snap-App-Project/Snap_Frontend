package com.example.snapproject

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.viewholder.DetailViewHolder

class DetailRecyclerViewAdapter(
    private val dataSet: Array<DetailItemData>
) : RecyclerView.Adapter<DetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        return DetailViewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return ViewType.valueOf(dataSet[position].viewType).ordinal
    }
}
