package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.ListRecyclerViewAdapter
import com.example.snapproject.MainActivity
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentListBinding
import com.example.snapproject.model.ListItemData
import com.example.snapproject.readText

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: ListRecyclerViewAdapter // RecyclerView 어댑터

    // 리스트에 넣을 더미 데이터 생성 -> ArrayList에 담기
    private val dataOne = ListItemData("1", "제품명1", "2021.11.20", false)
    private val dataTwo = ListItemData("2", "제품명2", "2022.11.20", false)
    private val dataThree = ListItemData("3", "제품명3", "2023.11.20", false)
    private val dataFour = ListItemData("4", "제품명4", "2024.11.20", false)
    private val dataFive = ListItemData("5", "제품명5", "2025.11.20", false)
    private val dataSix = ListItemData("6", "제품명6", "2026.11.20", false)
    private val dataSeven = ListItemData("7", "제품명7", "2027.11.20", false)
    private val dataEight = ListItemData("8", "제품명8", "2028.11.20", false)
    private val dataNine = ListItemData("9", "제품명9", "2029.11.20", false)
    private val dateTen = ListItemData("10", "제품명10", "2030.11.20", false)
    private val dataArray: ArrayList<ListItemData> =
        arrayListOf(dataOne, dataTwo, dataThree, dataFour, dataFive, dataSix, dataSeven, dataEight, dataNine, dateTen)

    companion object {
        fun newInstance() = ListFragment()
    }

    override fun onResume() {
        super.onResume()
        view?.post { // view가 생성된 후 실행
            binding.listLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS // 기존 Talkback focus 지우기

            // TTS 발화 먼저 진행 -> 발화 끝난 뒤, 다시 Talkback focus 복원
            MainActivity.tts.readText("소비기한별로 제품 리스트를 확인할 수 있습니다. 원하는 제품을 눌러 상세 정보를 확인해보세요.") {
                binding.listLayout.post { binding.listLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO }
            }
        }
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
                        itemId: Int,
                        position: Int,
                    ) {
                        // 제품 상세 설명 화면으로 이동 (Safe Args 전달 - "리스트 페이지에서 이동했음", 서버 응답은 null)
                        val action = ListFragmentDirections.actionListFragmentToDetailFragment(prevPage = "list", analyzeResponse = null)
                        findNavController().navigate(action)
                    }
                },
            )

            // 아이템 내부의 별(isFavorite) 클릭 리스너 연결
            recyclerViewAdapter.setStarClickListener(
                object : ListRecyclerViewAdapter.OnItemClickInterface {
                    override fun onItemClick(
                        v: View,
                        itemId: Int,
                        position: Int,
                    ) {
                        MainActivity.tts.readText("${itemId}번 별 클릭")
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
                    i.productId,
                    i.productName,
                    i.expirationDate,
                    i.isDeleteChecked,
                ),
            )
        }
        // 모든 Item이 추가된 Item 리스트를 UI에 반영
        recyclerViewAdapter.differ.submitList(itemList)
    }
}
