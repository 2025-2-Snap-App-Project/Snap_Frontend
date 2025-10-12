package com.example.snapproject.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.snapproject.LoadingFailureDialog
import com.example.snapproject.MainActivity
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentLoadingBinding
import com.example.snapproject.readText

class LoadingFragment : Fragment(), LoadingFailureDialog.LoadingFailureDialogListener {
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = LoadingFragment()
    }

    override fun onResume() {
        super.onResume()
        MainActivity.tts.readText("이미지 분석 진행 중입니다. 잠시만 기다려주세요.")
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
            }
        }

//        // 분석 실패 다이얼로그 show
//        val dialog = LoadingFailureDialog() // LoadingFailureDialog 인스턴스화
//        dialog.setTargetFragment(this, 0) // targetFragment Null 에러 방지
//        dialog.show(parentFragmentManager, "LoadingFailureDialog") // dialog 최종 show

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 촬영하기(카메라) 화면으로 이동
            findNavController().popBackStack()
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

    // Dialog 내부의 "재시도" 버튼 클릭 시
    override fun onDialogRetryClick(dialog: DialogFragment) { // dialog 사라짐
        dialog.dismiss()
    }

    // Dialog 내부의 "취소" 버튼 클릭 시
    override fun onDialogCancelClick(dialog: DialogFragment) { // dialog 사라짐, CameraFragment로 이동
        dialog.dismiss()
        findNavController().popBackStack()
    }
}
