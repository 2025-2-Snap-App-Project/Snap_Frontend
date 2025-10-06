package com.example.snapproject.Fragment

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.snapproject.HomeViewPagerAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentHomeBinding
import androidx.core.graphics.toColorInt

class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewPagerAdapter by lazy { HomeViewPagerAdapter(requireActivity() as FragmentActivity) }

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() = with(binding) {
        // 텍스트뷰에서 "사용자" 부분만 컬러 변경하기
        val tvData: String = tvWelcome.text.toString()
        val tvBuilder = SpannableStringBuilder(tvData)
        val colorBlueSpan = ForegroundColorSpan(
            "#2276FF".toColorInt())
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
