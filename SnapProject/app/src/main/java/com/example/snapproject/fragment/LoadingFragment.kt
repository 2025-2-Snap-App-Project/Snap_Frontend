package com.example.snapproject.fragment

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.snapproject.MainActivity
import com.example.snapproject.api.ApiRepository
import com.example.snapproject.api.ApiResult
import com.example.snapproject.databinding.FragmentLoadingBinding
import com.example.snapproject.readText
import com.example.snapproject.viewmodel.CameraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class LoadingFragment : Fragment() {
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    private var imgArrLst: ArrayList<File> = arrayListOf() // 이미지 파일 ArrayList
    private val viewModel by viewModels<CameraViewModel>() // 뷰모델 초기화

    companion object {
        fun newInstance() = LoadingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        // 뷰모델의 MutableMap (bitmap, category), 소비기한 String -> null 체크
        val bitmapMap = viewModel.bitmapMap.value
        val date = viewModel.expirationDate.value
        if (bitmapMap == null || date == null) {
            MainActivity.tts.readText("오류 발생, 다시 시도해주세요.", requireContext()) {
                findNavController().popBackStack()
                return@readText
            }
        }

        // 비트맵을 전부 File 타입으로 변경하여 ArrayList에 추가
        for ((bitmap, category) in bitmapMap!!) {
            // 파일명 지정을 위해, 카테고리 (제품명 / 제품 라벨)도 같이 넘겨줌
            val file = viewModel.saveBitmapToFile(bitmap, category, requireContext())
            imgArrLst.add(file) // 서버로 보낼 ArrayList에 생성된 파일 추가
            Log.d("fileName", "파일 생성 성공, 파일 경로 : ${file.toUri()}") // 파일 경로 확인
        }

        viewLifecycleOwner.lifecycleScope.launch { // Fragment의 뷰 생명 주기
            while (isActive) { // Fragment의 뷰가 살아있는 동안 계속 반복 (에러 발생 시, 서버 요청 무한 재시도)
                when (val result = ApiRepository.postAnalyze(imgArrLst, date!!)) { // result = 서버 요청 결과
                    is ApiResult.Success -> { // 서버 통신 성공 시
                        Log.d("LoadingFragment", "Success: $result")
                        // 제품 상세 설명 화면으로 이동 (Safe Args 전달 - "로딩 페이지에서 이동했음", 서버 응답)
                        val action =
                            LoadingFragmentDirections.actionLoadingFragmentToDetailFragment(
                                prevPage = "loading",
                                analyzeResponse = result.data,
                                itemId = -1,
                            )
                        findNavController().navigate(action)
                        return@launch // 리턴하여 반복문 빠져나옴.
                    }

                    is ApiResult.Error -> { // 서버 통신 실패 시
                        when (result.code) {
                            400 -> MainActivity.tts.readText("이미지 누락. 다시 이미지 분석을 시도합니다.", requireContext())
                            415 -> MainActivity.tts.readText("지원되지 않은 이미지 형식. 다시 이미지 분석을 시도합니다.", requireContext())
                            500 -> MainActivity.tts.readText("서버 오류 발생. 다시 이미지 분석을 시도합니다.", requireContext())
                            else -> MainActivity.tts.readText("알 수 없는 오류 발생. 다시 이미지 분석을 시도합니다.", requireContext())
                        }
                        Log.e(
                            "LoadingFragment",
                            "Error code: ${result.code}, message: ${result.message}",
                        )
                        delay(5000) // 딜레이 주고 서버 요청 재시도.
                    }
                }
            }
        }

        binding.btnHome.setOnClickListener { // "홈으로 돌아가기" 버튼 클릭 -> 홈 화면으로 이동
            findNavController().popBackStack()
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
