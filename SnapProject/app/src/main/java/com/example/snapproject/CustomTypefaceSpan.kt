package com.example.snapproject

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

// 원하는 폰트를 적용하는 커스텀 클래스
class CustomTypefaceSpan(private val typeface: Typeface?) : MetricAffectingSpan() {
    override fun updateDrawState(paint: TextPaint) {
        paint.typeface = typeface
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = typeface
    }
}
