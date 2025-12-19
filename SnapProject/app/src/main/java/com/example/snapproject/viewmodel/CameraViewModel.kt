package com.example.snapproject.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.snapproject.yolo.DataProcess
import com.example.snapproject.yolo.YoloResult

class CameraViewModel : ViewModel() {
    var runYOLO = true // true면 YOLO, false면 OCR

    // YOLO 추론 후, OCR 결과를 저장할 변수
    // 1. 제품명 OCR 결과
    private val _productName = MutableLiveData<String?>()
    val productName: LiveData<String?> = _productName

    // 2. 소비기한 OCR 결과
    private val _expirationDate = MutableLiveData<String?>()
    val expirationDate: LiveData<String?> = _expirationDate

    // 3. 제품 라벨
    private val _productLabel = MutableLiveData<Unit>()
    val productLabel: LiveData<Unit> = _productLabel

    // 인식 여부 플래그
    private val _isNameDetected = MutableLiveData(false)
    val isNameDetected: LiveData<Boolean> = _isNameDetected

    private val _isDateDetected = MutableLiveData(false)
    val isDateDetected: LiveData<Boolean> = _isDateDetected

    private val _isLabelDetected = MutableLiveData(false)
    val isLabelDetected: LiveData<Boolean> = _isLabelDetected

    // bitmap
    lateinit var nameBitmap: Bitmap
    lateinit var dateBitmap: Bitmap
    lateinit var labelBitmap: Bitmap

    // TTS 완료 플래그
    var isNameTTSCompleted: Boolean = false
    var isDateTTSCompleted: Boolean = false
    var isLabelTTSCompleted: Boolean = false

    // YOLO 추론 결과
    private val _yoloResults = MutableLiveData<ArrayList<YoloResult>>(arrayListOf())
    val yoloResults: LiveData<ArrayList<YoloResult>> = _yoloResults

    // 전체 화면 Bitmap
    var fullBitmap: Bitmap? = null
    var fullRotatedBitmap: Bitmap? = null

    // YOLO 추론 결과 + 전체 화면 Bitmap 업데이트
    fun onYoloResult(
        results: ArrayList<YoloResult>,
        fullBitmap: Bitmap,
        fullRotatedBitmap: Bitmap,
    ) {
        _yoloResults.postValue(results)
        this.fullBitmap = fullBitmap
        this.fullRotatedBitmap = fullRotatedBitmap
    }

    // 현재 POST 요청 중인지 여부를 알려주는 상태 변수
    var isRequesting = false

    lateinit var dataProcess: DataProcess

    // 제품 이름이 인식되었을 때
    fun onProductNameDetected(
        name: String,
        bitmap: Bitmap,
    ) {
        if (_isNameDetected.value == true) return

        _isNameDetected.value = true
        _productName.value = name
        nameBitmap = bitmap
    }

    // 소비기한이 인식되었을 때
    fun onExpirationDateDetected(
        date: String,
        bitmap: Bitmap,
    ) {
        if (_isDateDetected.value == true) return

        _isDateDetected.value = true
        _expirationDate.value = date
        dateBitmap = bitmap
    }

    // 제품 라벨이 인식되었을 때
    fun onProductLabelDetected(bitmap: Bitmap) {
        if (_isLabelDetected.value == true) return

        _isLabelDetected.value = true
        _productLabel.value = Unit
        labelBitmap = bitmap
    }

    // TTS 출력 완료 후, 관련 변수 업데이트
    fun onNameTTSCompleted() {
        isNameTTSCompleted = true
    }

    fun onDateTTSCompleted() {
        isDateTTSCompleted = true
    }

    fun onLabelTTSCompleted() {
        isLabelTTSCompleted = true
    }
}
