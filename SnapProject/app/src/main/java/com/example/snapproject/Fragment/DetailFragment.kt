package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentDetailBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.ListItemData

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val dataName = DetailItemData(0, "제품명은 **초코파이**입니다.")
    private val dataDate = DetailItemData(1, "소비기한은 **2025.07.22** 까지입니다.")
    private val dataStore = DetailItemData(2, "이 제품은 **냉장고 두 번째 칸**에 있습니다.")
    private val dataSummary =
        DetailItemData(3, "이 제품은 닭가슴살을 주재료로 한 가공식품입니다. 전반적으로 단백질이 풍부하지만, 몇 가지 주의할 점이 있습니다. 1. **대두(콩)**과 **밀**은 대표적인 알레르기 유발 성분입니다. 해당 알레르기가 있는 분은 섭취를 피하세요. 2. **혼합제제(폴리인산나트륨, 피로인산나트륨)**는 가공식품에서 보존성과 조직감을 높이기 위한 첨가물로, 과도한 섭취 시 신장 건강에 영향을 줄 수 있습니다. 3. **L-글루타민산나트륨(MSG)**는 감칠맛을 내는 조미료로, 일반적으로 안전하지만, 일부 민감한 사람에게는 두통 등을 유발할 수 있습니다.")

    private val dataArray: ArrayList<DetailItemData> =
        arrayListOf(dataName, dataDate, dataStore, dataSummary)

    companion object {
        fun newInstance() = ListFragment()
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
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
