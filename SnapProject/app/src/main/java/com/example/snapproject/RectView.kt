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
        previewWidth: Int,
        previewHeight: Int,
    ) {
        val modelSize = DataProcess.INPUT_SIZE.toFloat() // YOLO 모델 입력 이미지 크기
        val scale: Float // YOLO 모델 입력 이미지 좌표 -> PreviewView 좌표 변환 시, 곱하는 비율

        // 상하좌우 여백이 생기는 경우 -> 위치 조정을 위한 보정값 변수
        val offsetX: Float // 좌우 여백이 생기는 경우, X 좌표를 얼마나 이동할 건지
        val offsetY: Float // 상하 여백이 생기는 경우, Y 좌표를 얼마나 이동할 건지

        val previewRatio = previewWidth.toFloat() / previewHeight // PreviewView의 가로세로 비율
        val modelRatio = 1f // YOLO 모델 입력 이미지 가로세로 비율 = 1.0 (640 : 640)

        if (previewRatio > modelRatio) { // LandScape 형태일 때(위아래 여백 생김) 계산
            scale = previewWidth / modelSize
            val realHeight = modelSize * scale
            offsetX = 0f
            offsetY = (realHeight - previewHeight) / 2f
        } else { // Portrait 형태일 때(좌우 여백 생김) 계산
            scale = previewHeight / modelSize
            val realWidth = modelSize * scale
            offsetX = (realWidth - previewWidth) / 2f
            offsetY = 0f
        }

        // 좌표 변환 (YOLO 모델 입력 이미지 좌표 -> PreviewView 좌표)
        results.forEach { r ->
            r.rectF.left = r.rectF.left * scale - offsetX
            r.rectF.right = r.rectF.right * scale - offsetX
            r.rectF.top = r.rectF.top * scale - offsetY
            r.rectF.bottom = r.rectF.bottom * scale - offsetY
        }
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
