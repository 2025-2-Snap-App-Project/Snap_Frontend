package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentListBinding

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = ListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 홈 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnDelete.setOnClickListener { // 휴지통 버튼 클릭 -> 소비기한 리스트 삭제 화면으로 이동
            findNavController().navigate(R.id.action_listFragment_to_deleteFragment2)
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
