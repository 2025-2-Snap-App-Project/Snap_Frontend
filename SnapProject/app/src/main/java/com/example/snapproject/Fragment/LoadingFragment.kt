package com.example.snapproject.Fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.snapproject.R
import com.example.snapproject.databinding.DialogLoadingFailureBinding
import com.example.snapproject.databinding.FragmentLoadingBinding
import androidx.core.graphics.drawable.toDrawable
import com.example.snapproject.LoadingFailureDialog


class LoadingFragment : Fragment() {
    private var _binding: FragmentLoadingBinding? = null
    private val binding get() = _binding!!

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

        // 분석 실패 다이얼로그 show
        LoadingFailureDialog().show(parentFragmentManager, "LoadingFailureDialog")

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
}
