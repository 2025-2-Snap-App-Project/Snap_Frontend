package com.example.snapproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.snapproject.databinding.ItemListProductBinding
import com.example.snapproject.model.ListItemData

class ListRecyclerViewAdapter(
    private val mContext: Context,
) : RecyclerView.Adapter<ListRecyclerViewAdapter.ViewHolder>() {
    private var listener: OnItemClickInterface? = null // 아이템 클릭 리스너
    private var starListener: OnItemClickInterface? = null // 아이템 내부의 별(isFavorite) 클릭 리스너

    // 아이템 클릭 인터페이스
    interface OnItemClickInterface {
        fun onItemClick(
            v: View,
            itemId: String,
            position: Int,
        )
    }

    // 아이템 클릭 리스너
    fun setItemClickListener(listener: OnItemClickInterface) {
        this.listener = listener
    }

    //  // 아이템 내부의 별(isFavorite) 클릭 리스너
    fun setStarClickListener(listener: OnItemClickInterface) {
        this.starListener = listener
    }

    // DiffUtil 콜백 선언 (두 개의 리스트 간 차이 계산)
    private val differCallback =
        object : DiffUtil.ItemCallback<ListItemData>() {
            // ListItemData의 itemId를 통해 각각의 Item 식별
            // itemId를 비교 -> 같으면 areContentsTheSame으로 이동
            override fun areItemsTheSame(
                oldItem: ListItemData,
                newItem: ListItemData,
            ): Boolean {
                return oldItem.itemId == newItem.itemId
            }

            // ListItemData의 내용을 비교해서 같으면 true -> UI 변경 없음
            // ListItemData의 내용을 비교해서 다르면 false -> UI 변경
            override fun areContentsTheSame(
                oldItem: ListItemData,
                newItem: ListItemData,
            ): Boolean {
                return oldItem == newItem
            }
        }

    // 리스트가 많으면 백그라운드에서 실행하는 게 좋음 -> AsyncListDiffer은 자동으로 백그라운드에서 실행됨
    val differ = AsyncListDiffer(this, differCallback)

    // ViewHolder 클래스
    inner class ViewHolder(private val binding: ItemListProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listItemData: ListItemData) {
            // [Item의 요소 + ListItemData(data class)의 변수] 연결
            binding.apply {
                tvProductName.text = listItemData.productName
                tvExpirationDate.text = listItemData.expirationDate
            }

            // 소비기한 리스트 Item 클릭 리스너 등록
            binding.btnItem.setOnClickListener {
                listener?.onItemClick(binding.root, listItemData.itemId, adapterPosition)
            }

            // Item 내부의 별(isFavorite) 클릭 리스너 등록
            binding.btnIsFavorite.setOnClickListener {
                starListener?.onItemClick(binding.root, listItemData.itemId, adapterPosition)
            }


        }
    }

    // 소비기한 리스트 ViewHolder 생성
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ItemListProductBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    // 소비기한 리스트 ViewHolder 바인딩
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val todayData = differ.currentList[position]
        holder.bind(todayData)
    }

    // 소비기한 리스트 Item 개수 반환
    override fun getItemCount() = differ.currentList.size
}
