package com.example.snapproject.Fragment

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
import com.example.snapproject.R
import com.example.snapproject.api.ApiRepository
import com.example.snapproject.api.ApiResult
import com.example.snapproject.databinding.FragmentLoadingBinding
import com.example.snapproject.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class LoadingFragment : Fragment() {
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    private var imgArrLst: ArrayList<File> = arrayListOf() // 이미지 파일 ArrayList

    companion object {
        fun newInstance() = LoadingFragment()
    }

    override fun onResume() {
        super.onResume()
        view?.post { // view가 생성된 후 실행
            binding.loadingLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS // 기존 Talkback focus 지우기

            // TTS 발화 먼저 진행 -> 발화 끝난 뒤, 다시 Talkback focus 복원
            MainActivity.tts.readText("이미지 분석 진행 중입니다. 잠시만 기다려주세요.") {
                binding.loadingLayout.post { binding.loadingLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO }
            }
        }
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
                Log.d("LoadingFragment", "전달 받은 이미지 경로 : ${strUri.toUri()}") // Uri로 타입 변환 후, 경로 확인
                imgArrLst.add(File(strUri)) // 이미지 ArrayList에 이미지 파일 하나씩 추가
            }
        }

        lifecycleScope.launch {
            while (isActive) { // 서버 통신 에러 발생 시 -> 무한 재시도 (lifecycle이 살아있는 동안)
                when (val result = ApiRepository.postAnalyze(imgArrLst)) { // result = 서버 요청 결과
                    is ApiResult.Success -> { // 서버 통신 성공 시
                        // 제품 상세 설명 화면으로 이동
                        val action = LoadingFragmentDirections.actionLoadingFragmentToDetailFragment(prevPage = "loading")
                        findNavController().navigate(action)
                        return@launch // 리턴하여 반복문 빠져나옴.
                    }
                    is ApiResult.Error -> { // 서버 통신 실패 시
                        when (result.code) {
                            400 -> MainActivity.tts.readText("이미지 누락. 다시 이미지 분석을 시도합니다.")
                            415 -> MainActivity.tts.readText("지원되지 않은 이미지 형식. 다시 이미지 분석을 시도합니다.")
                            500 -> MainActivity.tts.readText("서버 오류 발생. 다시 이미지 분석을 시도합니다.")
                            else -> MainActivity.tts.readText("알 수 없는 오류 발생. 다시 이미지 분석을 시도합니다.")
                        }
                        delay(2000) // 2초 딜레이 주고 서버 요청 재시도.
                    }
                }
            }
        }

        binding.btnHome.setOnClickListener { // "홈으로 돌아가기" 버튼 클릭 -> 홈 화면으로 이동
            findNavController().navigate(R.id.action_loadingFragment_to_homeFragment)
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
