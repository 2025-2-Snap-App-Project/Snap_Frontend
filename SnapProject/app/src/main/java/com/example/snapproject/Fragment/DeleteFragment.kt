package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.DeleteRecyclerViewAdapter
import com.example.snapproject.MainActivity
import com.example.snapproject.ProductListHelper
import com.example.snapproject.databinding.FragmentDeleteBinding
import com.example.snapproject.model.ListItemData
import com.example.snapproject.readText
import com.google.android.material.tabs.TabLayout

class DeleteFragment : Fragment() {
    private var _binding: FragmentDeleteBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: DeleteRecyclerViewAdapter // RecyclerView 어댑터

    companion object {
        fun newInstance() = DeleteFragment()
    }

    override fun onResume() {
        super.onResume()
        view?.post { // view가 생성된 후 실행
            binding.deleteLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS // 기존 Talkback focus 지우기

            // TTS 발화 먼저 진행 -> 발화 끝난 뒤, 다시 Talkback focus 복원
            MainActivity.tts.readText("삭제하고 싶은 제품을 클릭하여 선택한 뒤, 하단의 삭제하기 버튼을 눌러주세요.") {
                binding.deleteLayout.post { binding.deleteLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO }
            }
        }
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

            val todayStr = ProductListHelper.getTodayDateStr() // 오늘 날짜 -> yyyy.MM.dd
            val sevenDaysLaterStr = ProductListHelper.getSevenDaysLaterDateStr() // 오늘로부터 7일 후 날짜 -> yyyy.MM.dd

            filterProductsBySelectedTab(todayStr, sevenDaysLaterStr) // 선택된 탭(날짜)을 기준으로 필터링된 제품 목록 조회

            // 삭제 리스트의 아이템 클릭 리스너 연결
            recyclerViewAdapter.setOnClickListener(
                object : DeleteRecyclerViewAdapter.OnItemClickInterface {
                    override fun onItemClick(
                        v: View,
                        itemId: Int,
                        position: Int,
                    ) {
                        MainActivity.tts.readText("${itemId}번 아이템이 선택되었습니다.")
                    }
                },
            )
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // 선택된 탭(날짜)을 기준으로 필터링된 제품 목록 조회
    private fun filterProductsBySelectedTab(todayStr: String, sevenDaysLaterStr: String) {
        // 화면 진입 시, 첫 번째 탭(날짜 지남) 선택 -> 날짜 지난 제품 목록 보여줌
        binding.tabLayoutCategory.post {
            binding.tabLayoutCategory.getTabAt(0)?.select() // 첫 번째 탭 선택
            val list = ProductListHelper.getListGone(requireContext(), todayStr) // 날짜 기준으로 필터링된 제품 목록 불러오기

            // ListItemData를 차례대로 생성하여, 리사이클러뷰 어댑터에 바뀐 내용 반영
            recyclerViewAdapter.differ.submitList(list.map { p -> ListItemData(p.productId, p.productName, p.expirationDate, false) })

            // 제품 개수를 UI에 반영
            binding.tvItemNum.text = "소비기한이 지난\n제품이 ${ProductListHelper.getCountGone(requireContext(), todayStr)}개입니다."
        }

        // 탭이 선택될 때마다, 해당 날짜에 맞는 제품 목록 보여줌
        binding.tabLayoutCategory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> { // 날짜 지난 제품 목록 보여줌
                            val list = ProductListHelper.getListGone(requireContext(), todayStr)
                            recyclerViewAdapter.differ.submitList(list.map { p -> ListItemData(p.productId, p.productName, p.expirationDate, false) })
                            binding.tvItemNum.text = "소비기한이 지난\n제품이 ${ProductListHelper.getCountGone(requireContext(), todayStr)}개입니다."
                        }
                        1 -> { // 날짜 임박 (7일 이하) 제품 목록 보여줌
                            val list = ProductListHelper.getListImminent(requireContext(), todayStr, sevenDaysLaterStr)
                            recyclerViewAdapter.differ.submitList(list.map { p -> ListItemData(p.productId, p.productName, p.expirationDate, false) })
                            binding.tvItemNum.text = "소비기한이 임박한\n제품이 ${ProductListHelper.getCountImminent(requireContext(), todayStr, sevenDaysLaterStr)}개입니다."
                        }
                        2 -> { // 날짜 여유 (7일 초과) 제품 목록 보여줌
                            val list = ProductListHelper.getListPlenty(requireContext(), sevenDaysLaterStr)
                            recyclerViewAdapter.differ.submitList(list.map { p -> ListItemData(p.productId, p.productName, p.expirationDate, false) })
                            binding.tvItemNum.text = "소비기한이 많이 남은\n제품이 ${ProductListHelper.getCountPlenty(requireContext(), sevenDaysLaterStr)}개입니다."
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
