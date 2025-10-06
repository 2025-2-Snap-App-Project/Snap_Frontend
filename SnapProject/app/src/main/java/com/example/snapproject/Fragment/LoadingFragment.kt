package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentLoadingBinding

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
