package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.DetailIngredientsDialog
import com.example.snapproject.DetailRecyclerViewAdapter
import com.example.snapproject.MainActivity
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentDetailBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.viewobject.DetailDateViewObject
import com.example.snapproject.model.viewobject.DetailNameViewObject
import com.example.snapproject.model.viewobject.DetailStorageViewObject
import com.example.snapproject.model.viewobject.DetailSummaryViewObject
import com.example.snapproject.readText

class DetailFragment : Fragment(), DetailIngredientsDialog.DetailIngredientsDialogListener {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: DetailRecyclerViewAdapter // RecyclerView 어댑터

    companion object {
        fun newInstance() = DetailFragment()
    }

    override fun onResume() {
        super.onResume()
        view?.post { // view가 생성된 후 실행
            binding.detailLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS // 기존 Talkback focus 지우기
        }
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

        val itemName = String.format(resources.getString(R.string.detail_item_name), "초코파이")
        val itemDate = String.format(resources.getString(R.string.detail_item_date), "2025.07.22")
        val itemStorage =
            String.format(resources.getString(R.string.detail_item_storage), "냉장고 두 번째 칸")
        val itemSummary =
            arrayListOf(
                "이 제품은 **닭가슴살**을 주재료로 한 가공식품입니다. 전반적으로 단백질이 풍부하지만, **몇 가지 주의할 점**이 있습니다. ",
                "1. **대두(콩)**과 **밀**은 대표적인 알레르기 유발 성분입니다.",
                "2. **혼합제제(폴리인산나트륨, 피로인산나트륨)**는 가공식품에서 보존성과 조직감을 높이기 위한 첨가물로, 과도한 섭취 시 신장 건강에 영향을 줄 수 있습니다.",
                "3. **L-글루타민산나트륨(MSG)**는 감칠맛을 내는 조미료로, 일반적으로 안전하지만, 일부 민감한 사람에게는 두통 등을 유발할 수 있습니다.",
            )
        val txtIngredients =
            "밀가루(밀:미국산,호주산), 마시멜로(물엿, 설탕, 젤라틴), 식물성유지(팜유), 설탕, 전란액, 코코아분말, 정제소금, 합성착향료(바닐린), " +
                "탄산수소나트륨(팽창제), 밀가루(밀:미국산,호주산), 마시멜로(물엿, 설탕, 젤라틴), 식물성유지(팜유), 설탕, 전란액, 코코아분말, " +
                "정제소금, 합성착향료(바닐린), 탄산수소나트륨(팽창제), 밀가루(밀:미국산,호주산), 마시멜로(물엿, 설탕, 젤라틴), 식물성유지(팜유), " +
                "설탕, 전란액, 코코아분말, 정제소금, 합성착향료(바닐린), 탄산수소나트륨(팽창제)"

        // 제품명, 소비기한, 보관 장소 아이템 -> 더미 데이터 ArrayList에 담기
        val dataArrayList: ArrayList<DetailItemData> =
            arrayListOf(
                DetailItemData("DETAIL_NAME", DetailNameViewObject(itemName)),
                DetailItemData("DETAIL_DATE", DetailDateViewObject(itemDate)),
            )

        // Safe Args로 받은 데이터 가져오기
        val args: DetailFragmentArgs by navArgs()
        val prevPage = args.prevPage

        if (prevPage == "list") { // 소비기한 리스트 화면에서 넘어온 경우
            dataArrayList.add(
                DetailItemData("DETAIL_STORAGE", DetailStorageViewObject(itemStorage)),
            ) // itemStorage (보관 장소 설명) 도 더미 데이터 ArrayList에 추가
            binding.tvStore.text = "보관 장소 수정" // 버튼 내부 텍스트 수정
        }

        for (summary in itemSummary) { // itemSummary의 element를 하나씩 더미 데이터 ArrayList에 추가
            dataArrayList.add(DetailItemData("DETAIL_SUMMARY", DetailSummaryViewObject(summary)))
        }

        recyclerViewAdapter = DetailRecyclerViewAdapter(dataArrayList) // RecyclerView 어댑터 생성

        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 소비기한 리스트 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnStore.setOnClickListener { // 보관하기 버튼 클릭 -> 보관하기(녹음) 화면으로 이동
            val action =
                DetailFragmentDirections.actionDetailFragmentToStoreRecordFragment(prevPage = prevPage) // 어떤 화면에서 넘어온 건지 args로 전달
            findNavController().navigate(action)
        }

        binding.btnMoreInfo.setOnClickListener {
            // 원재료명 다이얼로그 show
            val dialog =
                DetailIngredientsDialog(txtIngredients) // DetailIngredientsDialog 인스턴스화 (원재료명도 같이 입력으로 넣어줌)
            dialog.setTargetFragment(this, 0) // targetFragment Null 에러 방지
            dialog.show(parentFragmentManager, "DetailIngredientsDialog") // dialog 최종 show
        }

        binding.btnReplay.setOnClickListener { // 설명 다시 듣기 버튼 클릭 -> 제품 상세 설명 다시 들려줌
            val itemTexts = recyclerViewAdapter.getAllTextsForTTS(binding.recyclerview).joinToString(", ")
            MainActivity.tts.readText("제품 상세 설명입니다. $itemTexts")
        }
    }

    private fun initView() =
        with(binding) {
            // xml의 recyclerview와 앞서 만든 RecyclerView 어댑터 연결
            recyclerview.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerview.adapter = recyclerViewAdapter

            // RecyclerView 내부의 모든 아이템에 대해 Text를 가져옴
            val itemTexts = recyclerViewAdapter.getAllTextsForTTS(binding.recyclerview).joinToString(", ")

            // TTS 발화 먼저 진행 -> 발화 끝난 뒤, 다시 Talkback focus 복원
            MainActivity.tts.readText("제품 상세 설명입니다. $itemTexts") {
                binding.detailLayout.post { binding.detailLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Dialog 내부의 "닫기" 버튼 클릭 시
    override fun onDialogEditClick(dialog: DialogFragment) { // dialog 사라짐
        dialog.dismiss()
    }
}
