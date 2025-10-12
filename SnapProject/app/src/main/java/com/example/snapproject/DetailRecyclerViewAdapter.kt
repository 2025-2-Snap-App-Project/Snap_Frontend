package com.example.snapproject

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.viewholder.DetailViewHolder

class DetailRecyclerViewAdapter(
    private val dataSet: ArrayList<DetailItemData>,
) : RecyclerView.Adapter<DetailViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DetailViewHolder {
        return DetailViewHolderFactory.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(
        holder: DetailViewHolder,
        position: Int,
    ) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int {
        return ViewType.valueOf(dataSet[position].viewType).ordinal
    }

    // 모든 아이템의 내부 text를 한 번에 묶어서 반환하는 함수
    fun getAllTextsForTTS(parent: ViewGroup): List<String> {
        val texts = mutableListOf<String>()
        for (i in dataSet.indices) {
            val viewType = getItemViewType(i)
            val viewHolder = DetailViewHolderFactory.createViewHolder(parent, viewType)
            viewHolder.bind(dataSet[i])
            texts.add(viewHolder.getTextForTTS())
        }
        return texts
    }
}
