package com.example.snapproject.Fragment

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.snapproject.HomeViewPagerAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentHomeBinding
import com.example.snapproject.setTextColorAsLinearGradient

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: HomeViewPagerAdapter

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
        viewPagerAdapter = HomeViewPagerAdapter(this)

        initView()

        binding.btnTalkBack.setOnClickListener { // TalkBack 설정 버튼 클릭 시
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) // "시스템 설정 - 접근성"으로 이동
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

            // 텍스트뷰에서 "사용자" 부분만 컬러 변경하기
            val tvData: String = tvWelcome.text.toString()
            val tvBuilder = SpannableStringBuilder(tvData)
            val colorBlueSpan =
                ForegroundColorSpan(
                    "#2276FF".toColorInt(),
                )
            tvBuilder.setSpan(colorBlueSpan, 7, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvWelcome.text = tvBuilder

            // ViewPager2 어댑터 연결 + Indicator 붙이기
            viewPagerMenu.adapter = viewPagerAdapter
            viewPagerIndicator.attachTo(viewPagerMenu)
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
