package com.example.snapproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentHomeBinding
import com.example.snapproject.setTextColorAsLinearGradient

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        binding.btnCamera.setOnClickListener { // 카메라 버튼 클릭 시
            findNavController().navigate(R.id.action_homeFragment_to_cameraFragment)
        }
        binding.btnDate.setOnClickListener { // 소비기한 버튼 클릭 시
            findNavController().navigate(R.id.action_homeFragment_to_listFragment)
        }
        binding.btnAppInfo.setOnClickListener { // 앱 정보 확인 버튼 클릭 시
            // 라이선스를 확인할 수 있는 화면으로 이동
            val action = HomeFragmentDirections.actionHomeFragmentToLicenseFragment()
            findNavController().navigate(action)
        }
    }

    private fun initView() =
        with(binding) {
            // 앱 이름 텍스트뷰에 Gradient 적용
            val text = "SNAP"
            val mainBlue = ContextCompat.getColor(requireContext(), R.color.main_blue)
            val subBlueOne = ContextCompat.getColor(requireContext(), R.color.sub_blue_2)
            val subBlueTwo = ContextCompat.getColor(requireContext(), R.color.sub_blue_2)

            val colorArray = IntArray(3) { 0 }
            colorArray[0] = mainBlue
            colorArray[1] = subBlueOne
            colorArray[2] = subBlueTwo

            tvAppName.setTextColorAsLinearGradient(colorArray) // 미리 설정한 ColorArray로 Gradient 적용
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
