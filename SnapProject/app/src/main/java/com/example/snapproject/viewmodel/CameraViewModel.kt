package com.example.snapproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.snapproject.DataProcess
import com.example.snapproject.YoloResult

class CameraViewModel: ViewModel() {

    // YoloResults(클래스 인덱스 값, 신뢰도 점수, RectF) ArrayList를 저장할 변수들
    private val _yoloResults = MutableLiveData<ArrayList<YoloResult>>()

    val yoloResult: LiveData<ArrayList<YoloResult>>
        get() = _yoloResults

    // YoloResults의 RectF를 업데이트
    fun updateYoloResultsRectF(results: ArrayList<YoloResult>, previewWidth: Int, previewHeight: Int,) {
        val modelSize = DataProcess.INPUT_SIZE.toFloat() // YOLO 모델 입력 이미지 크기

        // YOLO 모델 입력 이미지 좌표 -> PreviewView 좌표 변환 시, 곱하는 비율
        val scaleX = previewWidth / modelSize
        val scaleY = previewHeight / modelSize

        // 좌표 변환 (YOLO 모델 입력 이미지 좌표 -> PreviewView 좌표)
        results.forEach { r ->
            r.rectF.left *= scaleX
            r.rectF.right *= scaleX
            r.rectF.top *= scaleY
            r.rectF.bottom *= scaleY
        }
        _yoloResults.postValue(results)
    }

}
