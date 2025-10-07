package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.ListRecyclerViewAdapter
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentDeleteBinding

class DeleteFragment : Fragment() {
    private var _binding: FragmentDeleteBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewAdapter: ListRecyclerViewAdapter // RecyclerView 어댑터

    companion object {
        fun newInstance() = DeleteFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewAdapter = ListRecyclerViewAdapter(requireContext()) // RecyclerView 어댑터 생성
        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 소비기한 리스트 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnDelete.setOnClickListener { // 삭제하기 버튼 클릭 -> 리스트에서 Item 삭제 로직 추가 필요
            findNavController().popBackStack() // 소비기한 리스트 화면으로 이동
        }
    }

    private fun initView() =
        with(binding) {
            // xml의 recyclerview와 앞서 만든 RecyclerView 어댑터 연결
            recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recyclerview.adapter = recyclerViewAdapter

        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
