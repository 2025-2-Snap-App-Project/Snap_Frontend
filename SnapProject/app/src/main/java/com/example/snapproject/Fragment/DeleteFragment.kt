package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snapproject.ListRecyclerViewAdapter
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
