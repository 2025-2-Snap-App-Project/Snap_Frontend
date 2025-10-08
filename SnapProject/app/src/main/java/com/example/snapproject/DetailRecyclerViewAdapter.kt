package com.example.snapproject

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.snapproject.databinding.ItemDetailProductBinding
import com.example.snapproject.model.DetailItemData

class DetailRecyclerViewAdapter (
    private val mContext: Context,
) : RecyclerView.Adapter<DetailRecyclerViewAdapter.ViewHolder>() {
    // DiffUtil 콜백 선언 (두 개의 리스트 간 차이 계산)
    private val differCallback =
        object : DiffUtil.ItemCallback<DetailItemData>() {
            // DetailItemData의 itemId를 통해 각각의 Item 식별
            // itemId를 비교 -> 같으면 areContentsTheSame으로 이동
            override fun areItemsTheSame(
                oldItem: DetailItemData,
                newItem: DetailItemData,
            ): Boolean {
                return oldItem.itemId == newItem.itemId
            }

            // DetailItemData의 내용을 비교해서 같으면 true -> UI 변경 없음
            // DetailItemData의 내용을 비교해서 다르면 false -> UI 변경
            override fun areContentsTheSame(
                oldItem: DetailItemData,
                newItem: DetailItemData,
            ): Boolean {
                return oldItem == newItem
            }
        }

    // 리스트가 많으면 백그라운드에서 실행하는 게 좋음 -> AsyncListDiffer은 자동으로 백그라운드에서 실행됨
    val differ = AsyncListDiffer(this, differCallback)

    // ViewHolder 클래스
    inner class ViewHolder(private val binding: ItemDetailProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(detailItemData: DetailItemData) {
            // [Item의 요소 + ListItemData(data class)의 변수] 연결
            binding.apply {
                tvDetail.text = detailItemData.detail
            }
        }
    }

    // 소비기한 리스트 ViewHolder 생성
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ItemDetailProductBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    // 소비기한 리스트 ViewHolder 바인딩
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val data = differ.currentList[position]
        holder.bind(data)
    }

    // 소비기한 리스트 Item 개수 반환
    override fun getItemCount() = differ.currentList.size
}
