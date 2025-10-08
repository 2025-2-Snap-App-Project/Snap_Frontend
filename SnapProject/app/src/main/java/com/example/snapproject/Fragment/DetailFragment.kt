package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = ListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 소비기한 리스트 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnStore.setOnClickListener { // 보관하기 버튼 클릭 -> 보관하기(녹음) 화면으로 이동
            findNavController().navigate(R.id.action_detailFragment_to_storeRecordFragment)
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
