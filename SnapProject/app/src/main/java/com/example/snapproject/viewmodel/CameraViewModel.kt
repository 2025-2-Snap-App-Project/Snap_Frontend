package com.example.snapproject.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.snapproject.yolo.DataProcess

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

    // 3가지 모두 인식되었는지 체크
    private val _isAllDetected = MediatorLiveData<Boolean>()
    val isAllDetected: LiveData<Boolean> = _isAllDetected

    init {
        _isAllDetected.value = false

        _isAllDetected.addSource(_isNameDetected) {
            _isAllDetected.value = isAllDetected()
        }
        _isAllDetected.addSource(_isDateDetected) {
            _isAllDetected.value = isAllDetected()
        }
        _isAllDetected.addSource(_isLabelDetected) {
            _isAllDetected.value = isAllDetected()
        }

    }

    private fun isAllDetected() : Boolean {
        if ((_isNameDetected.value == true) && (_isDateDetected.value == true) && (_isLabelDetected.value == true)) {
            return true
        }
        return false
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

}
