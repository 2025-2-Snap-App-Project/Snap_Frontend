package com.example.snapproject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.round

// YOLO 추론 결과를 화면에 보여줄 클래스 (View 상속)
class RectView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private var results: ArrayList<YoloResult>? = null
    private lateinit var classes: Array<String>

    // 텍스트 Paint 설정
    private val textPaint =
        Paint().also {
            it.textSize = 60f
            it.color = Color.WHITE
        }

    // 객체 박스 Paint 설정
    private val boxPaint =
        Paint().also {
            it.style = Paint.Style.STROKE
        }

    // 실제 기기의 화면 크기에 맞게 좌표값 수정
    fun transformRect(
        results: ArrayList<YoloResult>,
    ) {
        this.results = results
    }

    // 라벨링된 클래스의 문자열 값 가져오기
    fun setClassLabel(classes: Array<String>) {
        this.classes = classes
    }

    // OnDraw 오버라이드 (화면에 그리는 함수)
    override fun onDraw(canvas: Canvas) {
        results?.forEach {
            canvas.drawRect(it.rectF, boxPaint)
            canvas.drawText(
                classes[it.classIndex] + ", " + round(it.score * 100) + "%",
                it.rectF.left + 10,
                it.rectF.top + 60,
                textPaint,
            )
        }
        super.onDraw(canvas)
    }

    // 화면에 그려진 RectF 리턴
    fun getDrawRect(): RectF? {
        return results?.firstOrNull()?.rectF
    }
}
