package com.example.snapproject.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.snapproject.MainActivity
import com.example.snapproject.api.ApiRepository
import com.example.snapproject.api.ApiResult
import com.example.snapproject.databinding.FragmentLoadingBinding
import com.example.snapproject.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class LoadingFragment : Fragment() {
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    private var imgArrLst: ArrayList<File> = arrayListOf() // 이미지 파일 ArrayList

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

        // Safe Args로 받은 데이터 가져오기
        val args: LoadingFragmentArgs by navArgs()
        val uriArrLst = args.uriArrLst

        if (uriArrLst != null) {
            for (strUri in uriArrLst) {
                val uri = strUri.toUri() // String -> Uri로 변환
                Log.d("LoadingFragment", "전달 받은 이미지 경로 : $uri") // Uri로 타입 변환 후, 경로 확인
                imgArrLst.add(uriToFile(requireContext(), uri)) // 이미지 ArrayList에 이미지 파일 하나씩 추가
            }
        }

        viewLifecycleOwner.lifecycleScope.launch { // Fragment의 뷰 생명 주기
            while (isActive) { // Fragment의 뷰가 살아있는 동안 계속 반복 (에러 발생 시, 서버 요청 무한 재시도)
                when (val result = ApiRepository.postAnalyze(imgArrLst)) { // result = 서버 요청 결과
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
