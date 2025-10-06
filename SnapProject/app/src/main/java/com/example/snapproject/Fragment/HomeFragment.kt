package com.example.snapproject.Fragment

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
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
import androidx.fragment.app.FragmentActivity
import com.example.snapproject.HomeViewPagerAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewPagerAdapter by lazy { HomeViewPagerAdapter(requireActivity() as FragmentActivity) }

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
    }

    // LinearGradient를 적용하는 별도의 확장 메소드 정의
    private fun TextView.setTextColorAsLinearGradient(colors: IntArray) {
        if (colors.isEmpty()) {
            return
        }

        setTextColor(colors[0])
        this.paint.shader =
            LinearGradient(
                0f,
                0f,
                paint.measureText(this.text.toString()),
                -this.textSize,
                colors,
                null,
                Shader.TileMode.CLAMP,
            )
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

            tvAppName.setTextColorAsLinearGradient(colorArray)

            // 텍스트뷰에서 "사용자" 부분만 컬러 변경하기
            val tvData: String = tvWelcome.text.toString()
            val tvBuilder = SpannableStringBuilder(tvData)
            val colorBlueSpan =
                ForegroundColorSpan(
                    "#2276FF".toColorInt(),
                )
            tvBuilder.setSpan(colorBlueSpan, 7, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvWelcome.text = tvBuilder

            viewPagerMenu.adapter = viewPagerAdapter
            viewPagerIndicator.attachTo(viewPagerMenu)
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
