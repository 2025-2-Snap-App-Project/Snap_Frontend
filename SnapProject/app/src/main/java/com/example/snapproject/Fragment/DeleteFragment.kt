package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.DeleteRecyclerViewAdapter
import com.example.snapproject.ListRecyclerViewAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentDeleteBinding
import com.example.snapproject.model.ListItemData

class DeleteFragment : Fragment() {
    private var _binding: FragmentDeleteBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: DeleteRecyclerViewAdapter // RecyclerView 어댑터

    // 리스트에 넣을 더미 데이터 생성 -> ArrayList에 담기
    private val dataOne = ListItemData("1", "제품명1", "2021.11.20", false, false)
    private val dataTwo = ListItemData("2", "제품명2", "2022.11.20", true, false)
    private val dataThree = ListItemData("3", "제품명3", "2023.11.20", true, false)
    private val dataFour = ListItemData("4", "제품명4", "2024.11.20", false, false)
    private val dataFive = ListItemData("5", "제품명5", "2025.11.20", false, false)
    private val dataSix = ListItemData("6", "제품명6", "2026.11.20", true, false)
    private val dataSeven = ListItemData("7", "제품명7", "2027.11.20", false, false)
    private val dataEight = ListItemData("8", "제품명8", "2028.11.20", false, false)
    private val dataNine = ListItemData("9", "제품명9", "2029.11.20", true, false)
    private val dateTen = ListItemData("10", "제품명10", "2030.11.20", true, false)
    private val dataArray: ArrayList<ListItemData> =
        arrayListOf(dataOne, dataTwo, dataThree, dataFour, dataFive, dataSix, dataSeven, dataEight, dataNine, dateTen)

    companion object {
        fun newInstance() = DeleteFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewAdapter = DeleteRecyclerViewAdapter(requireContext()) // RecyclerView 어댑터 생성
        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 소비기한 리스트 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnDelete.setOnClickListener { // 삭제하기 버튼 클릭 -> 리스트에서 Item 삭제 로직 추가 필요
            findNavController().popBackStack() // 소비기한 리스트 화면으로 이동
        }
    }

    private fun initView() =
        with(binding) {
            // xml의 recyclerview와 앞서 만든 RecyclerView 어댑터 연결
            recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerview.adapter = recyclerViewAdapter

            addListItemData(dataArray) // 리스트 Item에 데이터 추가 (ArrayList에 담아둔 더미 데이터)
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
                    i.isDeleteChecked
                ),
            )
        }
        // 모든 Item이 추가된 Item 리스트를 UI에 반영
        recyclerViewAdapter.differ.submitList(itemList)
    }
}
