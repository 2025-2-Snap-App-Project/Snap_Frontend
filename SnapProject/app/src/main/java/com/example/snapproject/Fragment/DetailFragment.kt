package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.DetailRecyclerViewAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentDetailBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailDateViewObject
import com.example.snapproject.model.viewobject.DetailNameViewObject
import com.example.snapproject.model.viewobject.DetailStorageViewObject
import com.example.snapproject.model.viewobject.DetailSummaryViewObject

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: DetailRecyclerViewAdapter // RecyclerView 어댑터

    companion object {
        fun newInstance() = DetailFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)


        val itemName = String.format(resources.getString(R.string.detail_item_name),"초코파이")
        val itemDate = String.format(resources.getString(R.string.detail_item_date),"2025.07.22")
        val itemStorage = String.format(resources.getString(R.string.detail_item_storage),"냉장고 두 번째 칸")
        val itemSummary = arrayListOf(
            "이 제품은 **닭가슴살**을 주재료로 한 가공식품입니다. 전반적으로 단백질이 풍부하지만, **몇 가지 주의할 점**이 있습니다. ",
            "1. **대두(콩)**과 **밀**은 대표적인 알레르기 유발 성분입니다.",
            "2. **혼합제제(폴리인산나트륨, 피로인산나트륨)**는 가공식품에서 보존성과 조직감을 높이기 위한 첨가물로, 과도한 섭취 시 신장 건강에 영향을 줄 수 있습니다.",
            "3. **L-글루타민산나트륨(MSG)**는 감칠맛을 내는 조미료로, 일반적으로 안전하지만, 일부 민감한 사람에게는 두통 등을 유발할 수 있습니다."
        )

        // 제품명, 소비기한, 보관 장소 아이템 -> 더미 데이터 ArrayList에 담기
        val dataArrayList: ArrayList<DetailItemData> = arrayListOf(
            DetailItemData("DETAIL_NAME", DetailNameViewObject(itemName)),
            DetailItemData("DETAIL_DATE", DetailDateViewObject(itemDate)),
            DetailItemData("DETAIL_STORAGE", DetailStorageViewObject(itemStorage))
        )

        for (summary in itemSummary) { // itemSummary의 element를 하나씩 더미 데이터 ArrayList에 추가
            dataArrayList.add(DetailItemData("DETAIL_SUMMARY", DetailSummaryViewObject(summary)))
        }

        recyclerViewAdapter = DetailRecyclerViewAdapter(dataArrayList) // RecyclerView 어댑터 생성

        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 소비기한 리스트 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnStore.setOnClickListener { // 보관하기 버튼 클릭 -> 보관하기(녹음) 화면으로 이동
            findNavController().navigate(R.id.action_detailFragment_to_storeRecordFragment)
        }
    }

    private fun initView() =
        with(binding) {
            // xml의 recyclerview와 앞서 만든 RecyclerView 어댑터 연결
            recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerview.adapter = recyclerViewAdapter
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
