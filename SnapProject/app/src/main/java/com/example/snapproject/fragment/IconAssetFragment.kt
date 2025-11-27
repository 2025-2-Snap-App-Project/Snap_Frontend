package com.example.snapproject.fragment

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentIconAssetBinding

class IconAssetFragment : Fragment() {
    private var _binding: FragmentIconAssetBinding? = null
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
        _binding = FragmentIconAssetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // 아이콘 출처 표기
        binding.tvCamLicense.text = Html.fromHtml(getString(R.string.icon_asset_license_camera), Html.FROM_HTML_MODE_LEGACY)
        binding.tvCamLicense.movementMethod = LinkMovementMethod.getInstance()

        binding.tvCalLicense.text = Html.fromHtml(getString(R.string.icon_asset_license_calendar), Html.FROM_HTML_MODE_LEGACY)
        binding.tvCalLicense.movementMethod = LinkMovementMethod.getInstance()

        binding.tvMicroLicense.text = Html.fromHtml(getString(R.string.icon_asset_license_microphone), Html.FROM_HTML_MODE_LEGACY)
        binding.tvMicroLicense.movementMethod = LinkMovementMethod.getInstance()
    }
}
