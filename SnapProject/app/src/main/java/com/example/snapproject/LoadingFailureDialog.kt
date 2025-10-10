package com.example.snapproject

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.example.snapproject.databinding.DialogLoadingFailureBinding

class LoadingFailureDialog : DialogFragment() {
    private var _binding: DialogLoadingFailureBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLoadingFailureBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable()) // Dialog 배경의 Radius 값 반영을 위해 추가한 코드
        dialog?.setCanceledOnTouchOutside(false) // Dialog 바깥쪽 눌러도 취소 불가

        with(binding) {
            btnCancel.setOnClickListener { // 취소 버튼 클릭
                dialog?.dismiss()
            }
            btnRetry.setOnClickListener { // 재시도 버튼 클릭
                dialog?.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
