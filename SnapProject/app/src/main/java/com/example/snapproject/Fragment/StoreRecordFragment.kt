package com.example.snapproject.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.snapproject.databinding.FragmentStoreRecordBinding

class StoreRecordFragment : Fragment() {
    private var _binding: FragmentStoreRecordBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = StoreRecordFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStoreRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Safe Args로 받은 데이터 가져오기
        val args: StoreRecordFragmentArgs by navArgs()
        val prevPage = args.prevPage

        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 제품 상세 설명 화면으로 이동
            findNavController().popBackStack()
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
