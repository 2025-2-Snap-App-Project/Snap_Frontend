package com.example.snapproject.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.snapproject.databinding.FragmentLicenseBinding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity


class LicenseFragment : Fragment() {
    private var _binding: FragmentLicenseBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLicenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOssLicense.setOnClickListener { // Open Source Licenses 버튼 클릭 시
            startActivity(Intent(context, OssLicensesMenuActivity::class.java)) // 오픈소스 라이선스 목록 액티비티 보여줌
        }

        binding.btnIconAssetLicense.setOnClickListener { // Icon / Asset Licenses 버튼 클릭 시
            // IconAsset 프래그먼트로 이동
            val action = LicenseFragmentDirections.actionLicenseFragmentToIconAssetFragment()
            findNavController().navigate(action)
        }
    }
}
