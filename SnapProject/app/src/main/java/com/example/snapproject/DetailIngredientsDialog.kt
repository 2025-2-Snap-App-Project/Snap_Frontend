package com.example.snapproject

import android.content.Context
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.example.snapproject.databinding.DialogDetailIngredientsBinding

class DetailIngredientsDialog(txtIngredients: String) : DialogFragment() {
    private var _binding: DialogDetailIngredientsBinding? = null
    private val binding get() = _binding!!

    private val ingredients = txtIngredients // 원재료명

    // 부모 프래그먼트에 Event를 전달하기 위한 Listener
    private lateinit var listener: DetailIngredientsDialogListener

    // Linstener 인터페이스
    interface DetailIngredientsDialogListener {
        fun onDialogEditClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // LoadingFragment에 event 전달
            listener = targetFragment as DetailIngredientsDialogListener
        } catch (e: ClassCastException) {
            // 부모 프래그먼트에 인터페이스가 구현되지 않은 경우 예외 처리
            throw ClassCastException(
                (context.toString() + "must implement NoticeDialogListener"),
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DialogDetailIngredientsBinding.inflate(inflater, container, false)
        return (binding.root)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable()) // Dialog 배경의 Radius 값 반영을 위해 추가한 코드
        dialog?.setCanceledOnTouchOutside(false) // Dialog 바깥쪽 눌러도 취소 불가

        with(binding) {
            tvIngredients.text = ingredients.replace(" ", "\u00A0") // 입력으로 받은 ingredients(원재료명)으로 TextView의 내용 교체, Word Wrap 제거

            btnExit.setOnClickListener { // 닫기 버튼 클릭
                listener.onDialogEditClick(this@DetailIngredientsDialog)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // 디바이스 가로 크기 계산
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val deviceWidth = windowManager.currentWindowMetricsPointCompat().x

        // 디바이스 가로 크기의 80% 비율로 Dialog 너비 설정
        params?.width = (deviceWidth * 0.8).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 디바이스 화면 크기 구하는 함수
    private fun WindowManager.currentWindowMetricsPointCompat(): Point {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = currentWindowMetrics.windowInsets
            var insets: Insets = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            windowInsets.displayCutout?.run {
                insets =
                    Insets.max(
                        insets,
                        Insets.of(safeInsetLeft, safeInsetTop, safeInsetRight, safeInsetBottom),
                    )
            }
            val insetWidth = insets.right + insets.left
            val insetsHeight = insets.top + insets.bottom
            Point(
                currentWindowMetrics.bounds.width() - insetWidth,
                currentWindowMetrics.bounds.height() - insetsHeight,
            )
        } else {
            Point().apply {
                defaultDisplay.getSize(this)
            }
        }
    }
}
