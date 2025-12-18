package com.example.snapproject.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.snapproject.yolo.DataProcess
import com.example.snapproject.yolo.YoloResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CameraViewModel : ViewModel() {
    // YOLO 추론 후, OCR 결과를 저장할 변수
    // 1. 제품명 OCR 결과
    private val _productName = MutableLiveData<String?>()
    val productName: LiveData<String?> = _productName

    // 2. 소비기한 OCR 결과
    private val _expirationDate = MutableLiveData<String?>()
    val expirationDate: LiveData<String?> = _expirationDate

    // 3. 제품 라벨
    private val _productLabel = MutableLiveData<String?>()
    val productLabel: LiveData<String?> = _productLabel

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
    private var isNameTTSCompleted : Boolean = false
    private var isDateTTSCompleted : Boolean = false
    private var isLabelTTSCompleted : Boolean = false

    // 3개의 TTS를 모두 출력했는지
    private val _isAllTTSCompleted = MutableLiveData<Unit>()
    val isAllTTSCompleted: LiveData<Unit> = _isAllTTSCompleted


    // 3가지 모두 인식되었는지 체크
    private fun checkAllTTSCompleted() {
        if (isNameTTSCompleted && isDateTTSCompleted && isLabelTTSCompleted) {
            _isAllTTSCompleted.value = Unit
        }
    }

    // YOLO 추론 결과
    private val _yoloResults = MutableLiveData<ArrayList<YoloResult>>(arrayListOf())
    val yoloResults: LiveData<ArrayList<YoloResult>> = _yoloResults

    // 전체 화면 Bitmap
    var fullBitmap: Bitmap? = null
    var fullRotatedBitmap: Bitmap? = null

    // YOLO 추론 결과 + 전체 화면 Bitmap 업데이트
    fun onYoloResult(results: ArrayList<YoloResult>, fullBitmap: Bitmap, fullRotatedBitmap: Bitmap) {
        _yoloResults.postValue(results)
        this.fullBitmap = fullBitmap
        this.fullRotatedBitmap = fullRotatedBitmap
    }

    // 현재 POST 요청 중인지 여부를 알려주는 상태 변수
    var isRequesting = false

    // TTS 중복 실행 방지 플래그
    var isSpeaking = false

    lateinit var dataProcess: DataProcess

    // 제품 이름이 인식되었을 때
    fun onProductNameDetected(name: String, bitmap: Bitmap) {
        if (_isNameDetected.value == true) return

        _productName.postValue(name)
        _isNameDetected.postValue(true)
        nameBitmap = bitmap
    }

    // 소비기한이 인식되었을 때
    fun onExpirationDateDetected(date: String, bitmap: Bitmap) {
        if (_isDateDetected.value == true) return

        _expirationDate.postValue(date)
        _isDateDetected.postValue(true)
        dateBitmap = bitmap
    }

    // 제품 라벨이 인식되었을 때
    fun onProductLabelDetected(bitmap: Bitmap) {
        if (_isLabelDetected.value == true) return

        _productLabel.postValue("제품 라벨이 인식되었습니다.")
        _isLabelDetected.postValue(true)
        labelBitmap = bitmap
    }

    // TTS 출력 완료 후, 관련 변수 업데이트
    fun onNameTTSCompleted() {
        isNameTTSCompleted = true
        checkAllTTSCompleted()
    }
    fun onDateTTSCompleted() {
        isDateTTSCompleted = true
        checkAllTTSCompleted()
    }
    fun onLabelTTSCompleted() {
        isLabelTTSCompleted = true
        checkAllTTSCompleted()
    }
}
