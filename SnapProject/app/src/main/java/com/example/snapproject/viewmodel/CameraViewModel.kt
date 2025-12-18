package com.example.snapproject.viewmodel

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

    // 인식 여부 플래그
    private val _isNameDetected = MutableLiveData(false)
    val isNameDetected: LiveData<Boolean> = _isNameDetected

    private val _isDateDetected = MutableLiveData(false)
    val isDateDetected: LiveData<Boolean> = _isDateDetected

    private val _isLabelDetected = MutableLiveData(false)
    val isLabelDetected: LiveData<Boolean> = _isLabelDetected

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
    private var isRequesting = false

    // TTS 중복 실행 방지 플래그
    private var isSpeaking = false

    lateinit var dataProcess: DataProcess

    // 제품 이름이 인식되었을 때
    fun onProductNameDetected(name: String) {
        if (_isNameDetected.value == true) return

        _productName.postValue(name)
        _isNameDetected.postValue(true)
    }

    // 소비기한이 인식되었을 때
    fun onExpirationDateDetected(date: String) {
        if (_isDateDetected.value == true) return

        _expirationDate.postValue(date)
        _isDateDetected.postValue(true)
    }

    // 제품 라벨이 인식되었을 때
    fun onProductLabelDetected() {
        if (_isLabelDetected.value == true) return

        _isLabelDetected.postValue(true)
    }

    // 현재 TTS 출력 중인지 체크
    fun canSpeak(): Boolean {
        if (isSpeaking) return false
        isSpeaking = true
        return true
    }

    // TTS 종료
    fun isTTSFinished() {
        isSpeaking = false
    }

    // 네트워크 요청 상태 관리
    fun canRequest(): Boolean {
        if (isRequesting) return false
        isRequesting = true
        return true
    }
    fun isRequestFinished() {
        isRequesting = false
    }

}
