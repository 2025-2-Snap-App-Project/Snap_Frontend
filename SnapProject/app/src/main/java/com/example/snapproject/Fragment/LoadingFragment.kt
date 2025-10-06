package com.example.snapproject.Fragment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.snapproject.HomeViewPagerAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentHomeBinding
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
    }

    private fun initView() = with(binding) {
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
