package com.example.snapproject

import android.content.Context
import android.util.AttributeSet
import android.view.View

// YOLO 추론 결과를 화면에 보여줄 클래스 (View 상속)
class RectView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private var results: ArrayList<YoloResult>? = null
    private lateinit var classes: Array<String>

    // 실제 기기의 화면 크기에 맞게 좌표값 수정
    fun transformRect(results: ArrayList<YoloResult>) {
        val scaleX = width / DataProcess.INPUT_SIZE.toFloat()
        val scaleY = scaleX * 9f / 16f
        val realY = width * 9f / 16f
        val diffY = realY - height

        results.forEach {
            it.rectF.left *= scaleX
            it.rectF.right *= scaleX
            it.rectF.top = it.rectF.top * scaleY - (diffY / 2f)
            it.rectF.bottom = it.rectF.bottom * scaleY - (diffY / 2f)
        }
        this.results = results
    }

    // 라벨링된 클래스의 문자열 값 가져오기
    fun setClassLabel(classes: Array<String>) {
        this.classes = classes
    }
}
