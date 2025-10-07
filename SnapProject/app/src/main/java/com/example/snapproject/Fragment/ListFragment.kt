package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.ListRecyclerViewAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentListBinding
import com.example.snapproject.model.ListItemData

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter : ListRecyclerViewAdapter // RecyclerView 어댑터

    // 리스트에 넣을 더미 데이터 생성 -> ArrayList에 담기
    private val dataOne = ListItemData("1", "제품명1", "2020.11.20", false)
    private val dataTwo = ListItemData("2", "제품명2", "2021.11.20", false)
    private val dataThree = ListItemData("3", "제품명3", "2022.11.20", false)
    private val dataFour = ListItemData("4", "제품명4", "2023.11.20", false)
    private val dataArray: ArrayList<ListItemData> = arrayListOf(dataOne, dataTwo, dataThree, dataFour)

    companion object {
        fun newInstance() = ListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewAdapter = ListRecyclerViewAdapter(requireContext()) // RecyclerView 어댑터 생성
        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 홈 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnDelete.setOnClickListener { // 휴지통 버튼 클릭 -> 소비기한 리스트 삭제 화면으로 이동
            findNavController().navigate(R.id.action_listFragment_to_deleteFragment2)
        }
    }

    private fun initView() =
        with(binding) {
            // xml의 recyclerview와 앞서 만든 RecyclerView 어댑터 연결
            recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerview.adapter = recyclerViewAdapter

            addListItemData(dataArray) // 소비기한 리스트 Item에 데이터 추가 (ArrayList에 담아둔 더미 데이터)

            // 아이템 클릭 리스너 연결 (아이템 클릭 시, 상세 설명 화면으로 이동)
            recyclerViewAdapter.setItemClickListener(
                object : ListRecyclerViewAdapter.OnItemClickInterface {
                    override fun onItemClick(
                        v: View,
                        itemId: String,
                        position: Int,
                    ) {
                        Toast.makeText(context, "클릭한 아이템 ID : $itemId", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_listFragment_to_detailFragment)
                    }
                },
            )
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // 리사이클러뷰 Item에 데이터 추가 -> UI 업데이트
    private fun addListItemData(data: ArrayList<ListItemData>) {
        val itemList = ArrayList<ListItemData>(data.size)
        for (i in data) { // [입력으로 들어온 data <-> 리사이클러뷰 item data class] 매핑
            itemList.add(
                ListItemData(
                    i.itemId,
                    i.productName,
                    i.expirationDate,
                    i.isFavorite,
                ),
            )
        }
        // 모든 Item이 추가된 Item 리스트를 UI에 반영
        recyclerViewAdapter.differ.submitList(itemList)
    }
}
