package com.example.snapproject.yolo

import android.graphics.RectF

// 검출 객체 관련 data class
// 검출 객체 class 인덱스 값, confidence, 좌표값
data class YoloResult(val classIndex: Int, val score: Float, val rectF: RectF)
