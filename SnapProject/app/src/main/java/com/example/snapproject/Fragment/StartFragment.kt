package com.example.snapproject.Fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.snapproject.MainActivity
import com.example.snapproject.R
import com.example.snapproject.SnapApplication
import com.example.snapproject.databinding.FragmentStartBinding
import com.example.snapproject.readText
import com.example.snapproject.setTextColorAsLinearGradient

class StartFragment : Fragment() {
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = LoadingFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // SharedPreference에 시작하기 버튼이 클릭된 적이 있다고 기록되어 있다면 -> 시작하기 화면을 보여주지 않고, 바로 홈 화면으로 이동
        val btnStartClicked = SnapApplication.prefs.getBoolean("btn_start_clicked", false)
        if (btnStartClicked) findNavController().navigate(R.id.action_startFragment_to_homeFragment)

        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        Handler(Looper.getMainLooper()).postDelayed({
                MainActivity.tts?.readText("TalkBack을 키고 앱을 사용해주세요.")
        }, 500)

        binding.btnStart.setOnClickListener { // "시작하기" 버튼 클릭 -> 홈 화면으로 이동
            SnapApplication.prefs.setBoolean("btn_start_clicked", true) // "시작하기" 버튼이 클릭되었다고 SharedPreference에 기록
            findNavController().navigate(R.id.action_startFragment_to_homeFragment)
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
