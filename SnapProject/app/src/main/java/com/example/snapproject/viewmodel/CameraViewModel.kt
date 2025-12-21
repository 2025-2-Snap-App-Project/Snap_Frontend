package com.example.snapproject.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.snapproject.yolo.DataProcess
import com.example.snapproject.yolo.YoloResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

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

    // MutableMap -> (최종 인식된 cropped 비트맵, 카테고리)
    // 카테고리 - 제품명 or 제품 라벨
    var bitmapMap: MutableMap<Bitmap, String> = mutableMapOf()

    // 인식 여부 플래그
    private val _isNameDetected = MutableLiveData(false)
    val isNameDetected: LiveData<Boolean> = _isNameDetected

    private val _isDateDetected = MutableLiveData(false)
    val isDateDetected: LiveData<Boolean> = _isDateDetected

    private val _isLabelDetected = MutableLiveData(false)
    val isLabelDetected: LiveData<Boolean> = _isLabelDetected

    // YOLO 입력용 비트맵 변수
    lateinit var yoloBitmap: Bitmap

    // 인식된 cropped 비트맵
    lateinit var nameBitmap: Bitmap
    lateinit var labelBitmap: Bitmap

    // TTS 완료 플래그
    var isNameTTSCompleted: Boolean = false
    var isDateTTSCompleted: Boolean = false
    var isLabelTTSCompleted: Boolean = false

    // YOLO 추론 결과
    private val _yoloResults = MutableLiveData<ArrayList<YoloResult>>(arrayListOf())
    val yoloResults: LiveData<ArrayList<YoloResult>> = _yoloResults

    // 최근 OCR 날짜 후보 버퍼
    val dateBuffer = mutableListOf<String>()

    // YOLO 추론 결과 + 전체 화면 Bitmap 업데이트
    fun onYoloResult(
        results: ArrayList<YoloResult>,
        yoloBitmap: Bitmap,
    ) {
        _yoloResults.postValue(results)
        this.yoloBitmap = yoloBitmap
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
        addBitmap(bitmap, "name")
    }

    // 소비기한이 인식되었을 때
    fun onExpirationDateDetected(date: String) {
        if (_isDateDetected.value == true) return

        _isDateDetected.value = true
        _expirationDate.value = date
    }

    // 제품 라벨이 인식되었을 때
    fun onProductLabelDetected(bitmap: Bitmap) {
        if (_isLabelDetected.value == true) return

        _isLabelDetected.value = true
        _productLabel.value = Unit
        labelBitmap = bitmap
        addBitmap(bitmap, "label")
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

    // (인식된 비트맵, 카테고리) -> MutableMap에 추가
    private fun addBitmap(
        bitmap: Bitmap,
        category: String,
    ) {
        bitmapMap[bitmap] = category
    }

    // 비트맵을 File 타입으로 변경
    fun saveBitmapToFile(
        bitmap: Bitmap,
        category: String,
        context: Context,
    ): File {
        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA).format(System.currentTimeMillis()) + "-$category" // 파일명 설정
        val file = File(context.cacheDir, "$fileName.png") // File 객체 (캐시 directory에 저장)
        file.createNewFile()
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        return file
    }
}
