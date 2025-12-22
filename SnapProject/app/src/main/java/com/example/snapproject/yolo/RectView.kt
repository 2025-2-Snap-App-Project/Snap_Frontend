package com.example.snapproject.yolo

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
            it.color = Color.RED
            it.strokeWidth = 10f
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

    // PreviewView에 맞춰서 RectF(YOLO 객체) 좌표 변환
    fun transformRect(results: ArrayList<YoloResult>) {
        val inputSize = DataProcess.INPUT_SIZE.toFloat()

        // 세로 모드로 카메라 사용.
        // YOLO 좌표는 landscape 기준 → 회전 필요
        val scaleY = height / inputSize
        val scaleX = scaleY * 9f / 16f

        val realX = height * 9f / 16f
        val diffX = realX - width

        results.forEach {
            // 90도 회전 (x와 y를 서로 바꿔줌)
            val left = it.rectF.left
            val top = it.rectF.top
            val right = it.rectF.right
            val bottom = it.rectF.bottom

            val newTop = inputSize - right
            val newBottom = inputSize - left

            // rectF 좌표 변환
            it.rectF.left = top * scaleX - diffX / 2f
            it.rectF.right = bottom * scaleX - diffX / 2f
            it.rectF.top = newTop * scaleY
            it.rectF.bottom = newBottom * scaleY
        }

        this.results = results
    }

    // 화면에 그려진 RectF 리턴
    fun getDrawRect(): RectF? {
        return results?.firstOrNull()?.rectF
    }

    // 화면에 그려진 bbox 좌표가 화면 끝에 위치해있다면, false 반환
    fun isRectOnEdge(
        screenWidth: Float,
        screenHeight: Float,
    ): Boolean {
        val rectF = results?.firstOrNull()?.rectF ?: return false
        if (width == 0 || height == 0) return false

        // 화면의 5%
        val marginX = width * 0.05f
        val marginY = height * 0.05f

        return rectF.left <= marginX ||
            rectF.top <= marginY ||
            rectF.right >= screenWidth - marginX ||
            rectF.bottom >= screenHeight - marginY
    }
}
