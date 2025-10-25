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
import com.example.snapproject.model.db.Product
import com.example.snapproject.model.db.ProductDatabase
import com.example.snapproject.readText

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: ListRecyclerViewAdapter // RecyclerView 어댑터

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
    private fun addListItemData(data: List<Product>) {
        val itemList = ArrayList<ListItemData>(data.size)
        for (i in data) { // [입력으로 들어온 data <-> 리사이클러뷰 item data class] 매핑
            itemList.add(
                ListItemData(
                    i.productId,
                    i.productName,
                    i.expirationDate,
                    false,
                ),
            )
        }
        // 모든 Item이 추가된 Item 리스트를 UI에 반영
        recyclerViewAdapter.differ.submitList(itemList)
    }

    // 날짜 지난 제품 목록 보여줌
    fun showListGone(todayStr: String) {
        val productDB = ProductDatabase.getInstance(requireContext())
        val pastList = productDB?.productDao()?.getListGone(todayStr)
        if (pastList != null) {
            addListItemData(pastList)
        }
    }

    // 날짜 임박 (7일 이하) 제품 목록 보여줌
    fun showListImminent(todayStr: String, sevenDaysLaterStr: String) {
        val productDB = ProductDatabase.getInstance(requireContext())
        val imminentList = productDB?.productDao()?.getListImminent(todayStr, sevenDaysLaterStr)
        if (imminentList != null) {
            addListItemData(imminentList)
        }
    }

    // 날짜 여유 (7일 초과) 제품 목록 보여줌
    fun showListPlenty(sevenDaysLaterStr: String) {
        val productDB = ProductDatabase.getInstance(requireContext())
        val plentyList = productDB?.productDao()?.getListPlenty(sevenDaysLaterStr)
        if (plentyList != null) {
            addListItemData(plentyList)
        }
    }

}
