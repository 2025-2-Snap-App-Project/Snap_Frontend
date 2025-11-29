package com.example.snapproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.ProductListHelper
import com.example.snapproject.adapter.ListRecyclerViewAdapter
import com.example.snapproject.databinding.FragmentListBinding
import com.example.snapproject.model.ListItemData
import com.google.android.material.tabs.TabLayout

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: ListRecyclerViewAdapter // RecyclerView 어댑터

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
    }

    private fun initView() =
        with(binding) {
            // xml의 recyclerview와 앞서 만든 RecyclerView 어댑터 연결
            recyclerview.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerview.adapter = recyclerViewAdapter

            val todayStr = ProductListHelper.getTodayDateStr() // 오늘 날짜 -> yyyy.MM.dd
            val sevenDaysLaterStr =
                ProductListHelper.getSevenDaysLaterDateStr() // 오늘로부터 7일 후 날짜 -> yyyy.MM.dd

            filterProductsBySelectedTab(
                todayStr,
                sevenDaysLaterStr,
            ) // 선택된 탭(날짜)을 기준으로 필터링된 제품 목록 조회

            // 아이템 클릭 리스너 연결 (아이템 클릭 시, 상세 설명 화면으로 이동)
            recyclerViewAdapter.setItemClickListener(
                object : ListRecyclerViewAdapter.OnItemClickInterface {
                    override fun onItemClick(
                        v: View,
                        itemId: Int,
                        position: Int,
                    ) {
                        // 제품 상세 설명 화면으로 이동 (Safe Args 전달 - "리스트 페이지에서 이동했음", 서버 응답은 null, 클릭한 아이템의 ID)
                        val action =
                            ListFragmentDirections.actionListFragmentToDetailFragment(
                                prevPage = "list",
                                analyzeResponse = null,
                                itemId = itemId,
                            )
                        findNavController().navigate(action)
                    }
                },
            )
        }

    // 선택된 탭(날짜)을 기준으로 필터링된 제품 목록 조회
    private fun filterProductsBySelectedTab(
        todayStr: String,
        sevenDaysLaterStr: String,
    ) {
        // 화면 진입 시, 첫 번째 탭(날짜 지남) 선택 -> 날짜 지난 제품 목록 보여줌
        binding.tabLayoutCategory.post {
            binding.tabLayoutCategory.getTabAt(0)?.select() // 첫 번째 탭 선택
            val list =
                ProductListHelper.getListGone(requireContext(), todayStr) // 날짜 기준으로 필터링된 제품 목록 불러오기

            // ListItemData를 차례대로 생성하여, 리사이클러뷰 어댑터에 바뀐 내용 반영
            recyclerViewAdapter.differ.submitList(
                list.map { p ->
                    ListItemData(
                        p.productId,
                        p.productName,
                        p.expirationDate,
                        false,
                    )
                },
            )
        }

        // 탭이 선택될 때마다, 해당 날짜에 맞는 제품 목록 보여줌
        binding.tabLayoutCategory.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        when (it.position) {
                            0 -> { // 날짜 지난 제품 목록 보여줌
                                val list = ProductListHelper.getListGone(requireContext(), todayStr)
                                recyclerViewAdapter.differ.submitList(
                                    list.map { p ->
                                        ListItemData(
                                            p.productId,
                                            p.productName,
                                            p.expirationDate,
                                            false,
                                        )
                                    },
                                )
                            }

                            1 -> { // 날짜 임박 (7일 이하) 제품 목록 보여줌
                                val list =
                                    ProductListHelper.getListImminent(
                                        requireContext(),
                                        todayStr,
                                        sevenDaysLaterStr,
                                    )
                                recyclerViewAdapter.differ.submitList(
                                    list.map { p ->
                                        ListItemData(
                                            p.productId,
                                            p.productName,
                                            p.expirationDate,
                                            false,
                                        )
                                    },
                                )
                            }

                            2 -> { // 날짜 여유 (7일 초과) 제품 목록 보여줌
                                val list =
                                    ProductListHelper.getListPlenty(
                                        requireContext(),
                                        sevenDaysLaterStr,
                                    )
                                recyclerViewAdapter.differ.submitList(
                                    list.map { p ->
                                        ListItemData(
                                            p.productId,
                                            p.productName,
                                            p.expirationDate,
                                            false,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
