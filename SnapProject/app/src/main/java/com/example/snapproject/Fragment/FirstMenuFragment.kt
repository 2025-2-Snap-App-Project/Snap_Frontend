package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.snapproject.HomeViewPagerAdapter
import com.example.snapproject.databinding.FragmentFirstMenuBinding

class FirstMenuFragment : Fragment() {
    private var _binding : FragmentFirstMenuBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = FirstMenuFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
