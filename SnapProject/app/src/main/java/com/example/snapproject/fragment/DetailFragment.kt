package com.example.snapproject.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.snapproject.DetailIngredientsDialog
import com.example.snapproject.MainActivity
import com.example.snapproject.R
import com.example.snapproject.adapter.DetailRecyclerViewAdapter
import com.example.snapproject.databinding.FragmentDetailBinding
import com.example.snapproject.model.DetailItemData
import com.example.snapproject.model.db.ProductDatabase
import com.example.snapproject.readText
import com.example.snapproject.viewmodel.CameraViewModel

class DetailFragment : Fragment(), DetailIngredientsDialog.DetailIngredientsDialogListener {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewAdapter: DetailRecyclerViewAdapter // RecyclerView 어댑터

    // ArrayList 변수 (제품 기본 정보, 제품 요약 설명)
    private lateinit var basicInfoArrLst: ArrayList<String>
    private lateinit var summaryInfoArrLst: ArrayList<String>

    private val viewModel: CameraViewModel by activityViewModels() // CameraViewModel 공유

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

        // RecyclerView 아이템 ArrayList
        val dataArrayList: ArrayList<DetailItemData> = arrayListOf()

        // Safe Args로 받은 데이터 가져오기
        val args: DetailFragmentArgs by navArgs()
        val prevPage = args.prevPage
        val response = args.analyzeResponse
        val itemId = args.itemId // 현재 제품의 ID 가져오기 (Safe Args)

        if (prevPage == "loading") { // 이전 화면이 로딩 화면인 경우
            // TalkBack의 contentDescription 설정
            binding.btnBasicInfo.contentDescription = "제품명, 소비기한 정보 듣기 버튼"
            binding.btnBack.contentDescription = "홈으로 돌아가기 버튼. 홈 화면으로 다시 이동합니다."
            binding.btnStoreOrDelete.contentDescription = "제품 보관하기 버튼. 보관하기 화면으로 이동합니다."

            // SafeArgs로 받은 서버 응답 결과를 각각 변수에 저장
            val itemName = String.format(resources.getString(R.string.detail_item_name), response?.data?.productName)
            val itemDate = String.format(resources.getString(R.string.detail_item_date), viewModel.expirationDate.value)

            // 제품 기본 정보 ArrayList 초기화
            basicInfoArrLst = arrayListOf(itemName, itemDate)

            val itemSummary = response?.data?.summary

            if (itemSummary != null) { // 제품 요약 정보가 null이 아니라면
                summaryInfoArrLst = itemSummary as ArrayList<String> // 제품 요약 설명 ArrayList 초기화
            } else {
                MainActivity.tts.readText("제품 상세 정보가 누락되었습니다.", requireContext())
            }
        }

        if (prevPage == "list") { // 소비기한 리스트 화면에서 넘어온 경우
            binding.tvStoreOrDelete.text = "제품 삭제하기" // 버튼 내부 텍스트 수정
            binding.tvBack.text = "이전 화면으로 이동"

            // TalkBack의 contentDescription 설정
            binding.btnBasicInfo.contentDescription = "제품명, 소비기한, 보관 장소 정보 듣기 버튼"
            binding.btnBack.contentDescription = "이전 화면으로 이동 버튼. 소비기한 리스트 화면으로 다시 이동합니다."
            binding.btnStoreOrDelete.contentDescription = "제품 삭제 버튼. 해당 제품을 삭제하고 소비기한 리스트 화면으로 다시 이동합니다."

            // DB에서 해당 제품에 대한 상세 정보 불러오기
            val productDB = ProductDatabase.getInstance(requireContext())
            val detailData = productDB?.productDao()?.getDetail(itemId)

            if (detailData != null) { // DB에서 불러온 정보가 null이 아니라면
                // DB에서 가져온 내용을 각각 변수에 저장
                val itemName = String.format(resources.getString(R.string.detail_item_name), detailData.productName) // 제품명
                val itemDate = String.format(resources.getString(R.string.detail_item_date), detailData.expirationDate) // 소비기한
                val itemStorage = String.format(resources.getString(R.string.detail_item_storage), detailData.storageLocation) // 보관 장소
                val itemSummary = detailData.summary // 제품 요약 설명

                // 제품 기본 정보, 제품 요약 설명 ArrayList 각각 초기화
                basicInfoArrLst = arrayListOf(itemName, itemDate, itemStorage)
                summaryInfoArrLst = itemSummary as ArrayList<String>
            } else { // DB에서 불러온 정보가 null이라면
                MainActivity.tts.readText("제품 상세 정보를 불러올 수 없습니다!", requireContext())
            }
        }

        recyclerViewAdapter = DetailRecyclerViewAdapter(dataArrayList) // RecyclerView 어댑터 생성

        initView()

        // 제품 기본 정보 버튼 클릭 이벤트 처리
        binding.btnBasicInfo.setOnClickListener {
            for (info in basicInfoArrLst)
                MainActivity.tts.readText(info, requireContext())
        }

        // 제품 요약 설명 버튼 클릭 이벤트 처리
        binding.btnSummaryInfo.setOnClickListener {
            for (info in summaryInfoArrLst)
                MainActivity.tts.readText(info, requireContext())
        }

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> "홈 화면" or "소비기한 리스트" 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnStoreOrDelete.setOnClickListener { // (보관하기 or 제품 삭제하기) 버튼 클릭 이벤트 처리
            if (prevPage == "list") { // [소비기한 리스트 -> 제품 상세 설명]으로 화면 이동한 경우
                try {
                    // 해당 제품 삭제 후, 이전 화면으로 이동
                    val productDB = ProductDatabase.getInstance(requireContext())
                    productDB?.productDao()?.deleteProduct(itemId)
                    MainActivity.tts.readText("제품 삭제 성공", requireContext())
                    findNavController().popBackStack() // 소비기한 리스트 화면으로 이동
                } catch (e: Exception) {
                    // 제품 삭제 실패 시 TTS 출력
                    MainActivity.tts.readText("제품 삭제에 실패했습니다. 다시 시도해주세요.", requireContext())
                    Log.e("deleteProduct", "${e.message}")
                }
            } else { // [촬영하기 -> 제품 상세 설명]으로 화면 이동한 경우
                // 보관하기(녹음) 화면으로 이동
                val action =
                    DetailFragmentDirections.actionDetailFragmentToStoreRecordFragment(
                        prevPage = prevPage,
                        analyzeResponse = response,
                        itemId = itemId,
                    ) // "어떤 화면에서 넘어온 건지 + 서버 응답 결과" -> args로 전달
                findNavController().navigate(action)
            }
        }

        binding.btnMoreInfo.setOnClickListener {
            var txtIngredients: String? = null

            if (prevPage == "loading") { // 로딩 화면에서 넘어온 경우
                // Safe Args로 받은 서버 응답 결과 중, 원재료명 정보 가져오기
                txtIngredients = response?.data?.ingredients
            }

            if (prevPage == "list") { // 소비기한 리스트 화면에서 넘어온 경우
                // DB에서 해당 제품에 대한 원재료명 정보 읽어오기
                val itemId = args.itemId // 현재 제품의 ID 가져오기 (Safe Args)
                val productDB = ProductDatabase.getInstance(requireContext())
                val detailData = productDB?.productDao()?.getDetail(itemId)
                txtIngredients = detailData?.ingredients // 원재료명 정보
            }

            // 원재료명 다이얼로그 show
            if (txtIngredients != null) { // 원재료명 정보가 null이 아니라면
                MainActivity.tts.readText("제품 원재료명 정보입니다. $txtIngredients", requireContext())
            } else {
                MainActivity.tts.readText("원재료명 정보가 인식되지 않았습니다.", requireContext())
            }
        }
    }

    private fun initView() =
        with(binding) {
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
