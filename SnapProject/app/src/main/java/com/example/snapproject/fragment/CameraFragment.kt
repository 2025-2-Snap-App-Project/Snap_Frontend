package com.example.snapproject.fragment

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.R.attr.bitmap
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.snapproject.MainActivity
import com.example.snapproject.api.ApiRepository
import com.example.snapproject.api.ApiResult
import com.example.snapproject.databinding.FragmentCameraBinding
import com.example.snapproject.readText
import com.example.snapproject.yolo.DataProcess
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var mActivity: MainActivity

    private var cameraProvider: ProcessCameraProvider? = null // 카메라 프로바이더
    private var camera: Camera? = null // 카메라 객체
    private lateinit var preview: Preview // 카메라 미리보기 preview
    private var cameraFacing = CameraSelector.LENS_FACING_BACK // 후면 카메라를 기본값으로 설정
    private var imageCapture: ImageCapture? = null // 이미지 캡쳐를 위한 변수
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var imageAnalyzer: ImageAnalysis
    private var uriArrayList: ArrayList<String> = arrayListOf() // 이미지 파일 저장 경로 ArrayList

    private lateinit var dataProcess: DataProcess

    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions
    private var isRequesting = false // 현재 POST 요청 중인지 여부를 알려주는 상태 변수

    // TextRecognizer 인스턴스 생성
    val txtRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    // OrtSession 관련 변수
    private lateinit var ortEnvironment: OrtEnvironment
    private lateinit var session: OrtSession

    // YOLO 추론 후, OCR 결과를 저장할 변수
    private var productName: String? = null // 제품명 OCR 결과
    private var expirationDate: String? = null // 소비기한 OCR 결과
    private var productLabelTxt: String = "제품 라벨이 인식되었습니다."

    // 인식 여부를 저장할 변수
    @Volatile private var isNameDetected: Boolean = false

    @Volatile private var isDatedDetected: Boolean = false

    @Volatile private var isLabelDetected: Boolean = false

    // TTS로 안내한 횟수를 저장할 변수
    private var productNameTTSNum: Int = 0 // 제품명 TTS 횟수
    private var expirationDateTTSNum: Int = 0 // 소비기한 TTS 횟수
    private var productLabelTTSNum: Int = 0 // 제품 라벨 TTS 횟수

    // TTS 중복 실행 방지 플래그
    private var isSpeaking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun newInstance() = CameraFragment()

        // 필요한 권한 array 선언 및 초기화 (카메라 촬영)
        private val PERMISSIONS_REQUIRED =
            arrayOf(android.Manifest.permission.CAMERA)
    }

    // 앱 설정 Permission 콜백 등록 (앱 설정에서의 사용자 이벤트 처리)
    private val settingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!hasPermissions(mContext)) { // 사용자가 앱 설정에서도 권한 허용을 해주지 않은 경우
                MainActivity.tts.readText("카메라 권한을 허용해야 앱 사용이 가능합니다.", requireContext()) {
                    findNavController().popBackStack() // 홈 화면 이동
                }
            }
        }

    // 권한 있는지 없는지 검사하는 함수
    private fun hasPermissions(context: Context) =
        PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(
                context,
                it,
            ) == PackageManager.PERMISSION_GRANTED
        }

    // Permission 콜백 등록 (권한 요청 다이얼로그에서의 사용자 이벤트 처리)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted =
                permissions.all { it.value } // allGranted : 권한 요청 다이얼로그 창에서 모두 허용했는지 (true/false)
            if (!allGranted) { // 하나라도 거부한 경우
                // 사용자가 다시 묻지 않음을 선택한 경우 -> shouldShowRequestPermissionRationale이 false 반환 -> noAskAgain이 true가 됨.
                val noAskAgain =
                    PERMISSIONS_REQUIRED.any { permission ->
                        ContextCompat.checkSelfPermission(
                            requireContext(),
                            permission,
                        ) == PackageManager.PERMISSION_DENIED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(
                                mActivity,
                                permission,
                            )
                    }
                if (noAskAgain) { // 사용자가 다시 묻지 않음을 선택한 경우 -> 앱 설정 화면으로 이동
                    MainActivity.tts.readText("앱 설정에서 카메라 권한을 허용해주세요.", requireContext())
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData("package:${mContext.packageName}".toUri())
                    settingPermissionLauncher.launch(intent)
                } else { // 사용자가 한 번만 거부한 경우
                    MainActivity.tts.readText("카메라 권한이 필요합니다.", requireContext()) {
                        findNavController().popBackStack() // 홈 화면 이동
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    // context와 activity 가져오는 함수
    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
        mActivity = context as MainActivity

        // mContext 초기화된 뒤에, DataProcess 객체 생성
        dataProcess = DataProcess(context = mContext)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    // 시스템 설정에서 권한 허용해 준 뒤, 다시 돌아왔을 때 카메라 세팅 필요
    override fun onResume() {
        super.onResume()
        if (hasPermissions(mContext)) {
            load() // onnx + 라벨링 txt 파일 불러오기, OrtSession 객체 생성
            setUpCamera() // Camera 세팅
        }
    }

    private fun initView() =
        with(binding) {
            // 2개의 권한이 모두 허용된 상태가 아니라면 -> 권한 요청 Dialog 띄우기
            if (!hasPermissions(mContext)) {
                requestPermissionLauncher.launch(PERMISSIONS_REQUIRED)
            } else {
                setUpCamera()
            }
        }

    // 카메라 설정하는 함수
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // 카메라 리소스(Preview, ImageAnalysis 등) 설정 및 바인딩
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(mContext),
        )
    }

    // UseCase(Preview) 설정 및 바인딩
    private fun bindCameraUseCases() {
        // CameraProvider
        val cameraProvider =
            cameraProvider
                ?: throw IllegalStateException("Camera initialization failed.")

        // 후면 카메라로 설정
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // 카메라 Preview 설정
        preview =
            Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.previewCamera.surfaceProvider
                }

        // 이미지 캡쳐 Builder 객체 생성
        imageCapture = ImageCapture.Builder().build()

        // 이미지 분석을 위한 ImageAnalysis 객체 생성 및 세팅
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        imageAnalyzer.setAnalyzer(cameraExecutor) {
            imageProcess(it)
            it.close()
        }

        // 기존에 연결되어 있던 use-cases 우선 해제(unbind)
        cameraProvider.unbindAll()

        try {
            // 카메라와 연결할 수명 주기 자동 생성
            // bindToLifeCycle을 통해 사용자 기기와 카메라 Provider는 동일한 생명 주기를 갖게 됨.
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer,
            )
        } catch (exc: Exception) {
            Log.e("CameraFragment", "Use case binding failed", exc)
        }
    }

    // 비트맵을 캐시 디렉터리에 이미지 파일 형태로 저장하는 함수
    private fun saveImgFile(
        category: String,
        bitmap: Bitmap,
    ): Uri {
        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA).format(System.currentTimeMillis()) + "-$category" // 파일명 설정
        val imgFile = File(requireContext().cacheDir, "$fileName.png") // File 객체 (캐시 directory에 저장)
        imgFile.createNewFile() // 파일 생성
        val outputStream = FileOutputStream(imgFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // 이미지 저장
        outputStream.close()
        Log.d("CameraFragment", "저장된 파일 경로 : ${imgFile.toUri()}") // 이미지 저장 경로 확인
        return imgFile.toUri()
    }

    // 저장된 이미지 파일 경로를 ArrayList에 추가
    private fun addUriArrayList(uri: Uri) {
        uriArrayList.add(uri.toString())
    }

    // 이미지 처리 함수
    private fun imageProcess(imageProxy: ImageProxy) {
        binding ?: return // binding이 null이면 바로 리턴 (다음 화면 이돋 시 발생하는 NullPointerException 에러 방지)

        val rotation = imageProxy.imageInfo.rotationDegrees // 현재 이미지 회전 각도 가져오기

        val bitmap = dataProcess.imageToBitmap(imageProxy) // 비트맵 이미지
        val rotatedBitmap = dataProcess.imageToRotatedBitmap(bitmap, rotation) // 회전된 비트맵 이미지

        val floatBuffer = dataProcess.bitmapToFloatBuffer(rotatedBitmap)
        val inputName = session.inputNames.iterator().next()

        // 모델 요구 입력값 (배치 사이즈, 픽셀, 너비, 높이)
        val shape =
            longArrayOf(
                DataProcess.BATCH_SIZE.toLong(),
                DataProcess.PIXEL_SIZE.toLong(),
                DataProcess.INPUT_SIZE.toLong(),
                DataProcess.INPUT_SIZE.toLong(),
            )

        // YOLO 추론 코드
        val inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBuffer, shape)
        val resultTensor = session.run(Collections.singletonMap(inputName, inputTensor))
        val outputs = resultTensor.get(0).value as Array<*>

        // YOLO 추론 최종 결과 출력
        val results = dataProcess.outputsToNPMSPredictions(outputs) // YOLO 추론 최종 결과를 result에 저장
        binding.rectView.transformRect(results, binding.previewCamera.width, binding.previewCamera.height) // 실제 기기 화면 크기에 맞게 좌표값 조정
        binding.rectView.invalidate() // 최종 결과를 화면에 그려줌

        val drawRect = binding.rectView.getDrawRect() // 화면에 그려진 Rect 가져오기
        val fullBitmap = imageProxy.toBitmap() // 원본 imageProxy를 비트맵으로
        val fullRotatedBitmap = imageToRotatedBitmap(imageProxy.toBitmap(), rotation) // 원본 imageProxy를 회전된 비트맵으로
        val screenBitmap = createScreenBitmap(fullRotatedBitmap) // 현재 스크린에 보이는 만큼 비트맵 생성

        if (drawRect != null) { // drawRect가 화면에 표시된 상태라면
            val drawRectBitmap = createRectBitmap(screenBitmap, drawRect) // RectView 크기만큼 비트맵 생성
            val imgFile = saveBitmapToFile(drawRectBitmap) // RectView 크기의 비트맵을 File(.png)로 저장

            if (results.firstOrNull()?.classIndex == 1) {
                // 인식한 제품명 이미지를 서버로 전송하여 OCR 요청 -> 응답 결과 TTS 출력
                productNamePostAndTTS(imgFile, drawRectBitmap)
            }

            // "제품 라벨 인식됨" -> TTS 출력
            if (results.firstOrNull()?.classIndex == 0) {
                productLabelTTS(drawRectBitmap)
            }
        }

        // 소비기한 OCR 수행
        recognizeExpiryDate(fullBitmap)
    }

    // 회전된 비트맵 생성
    private fun imageToRotatedBitmap(
        bitmap: Bitmap,
        degrees: Int,
    ): Bitmap {
        // Matrix 객체에 매개변수로 받은 회전 각도 적용
        val matrix = android.graphics.Matrix()
        matrix.postRotate(degrees.toFloat())

        // 회전된 비트맵 반환
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // 현재 스크린 크기만큼 비트맵 생성
    private fun createScreenBitmap(fullBitmap: Bitmap): Bitmap {
        // PreviewView의 가로 세로 비율 계산
        val screenW = binding.previewCamera.width
        val screenH = binding.previewCamera.height
        val screenRatio = screenW.toFloat() / screenH.toFloat()

        // 원본 비트맵의 가로 세로 비율 계산
        val imgW = fullBitmap.width
        val imgH = fullBitmap.height
        val imgRatio = imgW.toFloat() / imgH.toFloat()

        var cropW = imgW
        var cropH = imgH

        if (imgRatio > screenRatio) { // 이미지가 가로로 더 넓음 → 좌우를 잘라야 함
            cropW = (imgH * screenRatio).toInt()
        } else { // 이미지가 세로로 더 김 → 위아래를 잘라야 함
            cropH = (imgW / screenRatio).toInt()
        }

        // 중앙에서 crop
        val left = (imgW - cropW) / 2
        val top = (imgH - cropH) / 2

        val croppedBitmap = Bitmap.createBitmap(fullBitmap, left, top, cropW, cropH)
        return croppedBitmap
    }

    // RectView 크기만큼 비트맵 생성
    private fun createRectBitmap(
        screenBitmap: Bitmap,
        drawRect: RectF,
    ): Bitmap {
        // screenBitmap의 실제 크기
        val imgW = screenBitmap.width
        val imgH = screenBitmap.height

        // PreviewView 실제 화면에서의 크기
        val viewW = binding.previewCamera.width
        val viewH = binding.previewCamera.height

        // 화면 Rect → 비트맵 좌표 변환 시 곱해줄 값
        val scaleX = imgW.toFloat() / viewW.toFloat()
        val scaleY = imgH.toFloat() / viewH.toFloat()

        // 화면 Rect → 비트맵 좌표로 변환
        val left = (drawRect.left * scaleX).toInt()
        val top = (drawRect.top * scaleY).toInt()
        val right = (drawRect.right * scaleX).toInt()
        val bottom = (drawRect.bottom * scaleY).toInt()

        // 좌표와 크기가 비트맵을 벗어나지 않도록 강제로 제한
        val cropLeft = left.coerceIn(0, imgW - 1)
        val cropTop = top.coerceIn(0, imgH - 1)
        val cropWidth = (right - left).coerceAtLeast(1).coerceAtMost(imgW - cropLeft)
        val cropHeight = (bottom - top).coerceAtLeast(1).coerceAtMost(imgH - cropTop)

        Log.d("bitmapSize", "left=$cropLeft top=$cropTop width=$cropWidth height=$cropHeight")

        // 최종 rect 비트맵 생성
        val rectBitmap =
            Bitmap.createBitmap(
                screenBitmap,
                cropLeft,
                cropTop,
                cropWidth,
                cropHeight,
            )

        return rectBitmap
    }

    // 비트맵 이미지를 File 타입으로 바꿔서 저장
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA).format(System.currentTimeMillis()) // 파일명 설정
        val fileItem = File(requireContext().cacheDir, "$fileName.png") // File 객체 (캐시 directory에 저장)
        fileItem.createNewFile()
        val fos = FileOutputStream(fileItem)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        return fileItem
    }

    // 소비기한 OCR 수행
    private fun recognizeExpiryDate(bitmap: Bitmap) {
        // Bitmap 객체에서 InputImage 객체 생성
        val image = InputImage.fromBitmap(bitmap, 0)

        // OCR 수행
        txtRecognizer.process(image)
            .addOnSuccessListener { // OCR 성공 시, text를 로그로 출력
                Log.d("ocrRawTxt", "OCR raw text: '${it.text}'")
                val dates = extractValidDates(it.text) // 소비기한 조건 체크
                if (dates.isNotEmpty()) { // 소비기한이 인식된 경우
                    Log.d("ocrDateSuccess", "인식된 날짜: ${dates.first()}")
                    expirationDate = dates.first() // 인식된 소비기한을 변수에 저장
                    expiryDateTTS(bitmap) // 인식된 소비기한 TTS 출력
                } else { // 소비기한이 인식되지 않은 경우
                    Log.d("ocrDateEmpty", "소비기한이 인식되지 않음")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ocrDateError", "${e.message}")
            }
    }

    // 인식된 소비기한 TTS 출력
    private fun expiryDateTTS(bitmap: Bitmap) {
        if (isSpeaking || isDatedDetected) return

        isSpeaking = true
        isDatedDetected = true
        Log.d("CameraFragment", "isDatedDetected: $isDatedDetected")

        expirationDate?.let {
            MainActivity.tts.readText(it, requireContext()) {
                requireActivity().runOnUiThread {
                    val uri = saveImgFile("date", bitmap)
                    addUriArrayList(uri)
                    checkAllDetected() // 제품명, 소비기한, 라벨이 모두 인식되었는지 체크
                    isSpeaking = false
                }
            }
        }
    }

    // 인식된 제품명 이미지 서버로 POST 요청 + TTS 출력
    private fun productNamePostAndTTS(
        imgFile: File,
        bitmap: Bitmap,
    ) {
        if (isRequesting || isSpeaking || isNameDetected) return
        isRequesting = true
        isSpeaking = true

        // 서버 요청 + TTS 발화
        lifecycleScope.launch {
            when (val result = ApiRepository.postName(imgFile)) { // POST 요청
                is ApiResult.Success -> { // 성공한 경우 -> Log로 인식된 제품명 출력
                    productName = result.data.productName // 제품명 인식 결과 저장
                    isNameDetected = true
                    Log.d("CameraFragment", "isNameDetected: $isNameDetected")

                    MainActivity.tts.readText(productName!!, requireContext()) {
                        requireActivity().runOnUiThread {
                            val uri = saveImgFile("name", bitmap)
                            addUriArrayList(uri)
                            checkAllDetected() // 제품명, 소비기한, 라벨이 모두 인식되었는지 체크
                            isSpeaking = false // TTS가 끝나는 시점에 false로 바꿔주기
                        }
                    }
                }
                is ApiResult.Error -> {
                    Log.e("productNameTTS", "서버 요청 실패")
                    isSpeaking = false // 서버 요청 실패한 경우에도 false로 바꿔주기
                }
            }
        }
    }

    // 인식된 라벨 TTS 출력
    private fun productLabelTTS(bitmap: Bitmap) {
        if (isSpeaking || isLabelDetected) return

        isSpeaking = true
        isLabelDetected = true
        Log.d("CameraFragment", "isLabelDetected: $isLabelDetected")

        MainActivity.tts.readText(productLabelTxt, requireContext()) {
            requireActivity().runOnUiThread {
                val uri = saveImgFile("label", bitmap)
                addUriArrayList(uri)
                checkAllDetected() // 제품명, 소비기한, 라벨이 모두 인식되었는지 체크
                isSpeaking = false
            }
        }
    }

    // 제품명, 소비기한, 라벨이 모두 인식되었는지 체크하는 함수
    private fun checkAllDetected() {
        // 3개 다 인식되었다면, 다음 화면으로 이동
        if (isNameDetected && isDatedDetected && isLabelDetected) {
            // 카메라 자원 해제
            cameraProvider?.unbindAll()
            cameraExecutor.shutdownNow()

            val action =
                CameraFragmentDirections.actionCameraFragmentToLoadingFragment(uriArrLst = uriArrayList.toTypedArray())
            findNavController().navigate(action)
        }
    }

    // OCR 수행 결과 -> 소비기한에 해당하는지 체크하는 함수
    private fun extractValidDates(text: String): List<String> {
        // 날짜 정규식: 2자리 또는 4자리 연도, 점(.) 또는 하이픈(-), 월/일 1~2자리
        val dateRegex = "\\b(\\d{2}|\\d{4})[.\\-]\\s*(\\d{1,2})[.\\-]\\s*(\\d{1,2})\\b".toRegex()

        // 최종 결과 반환용 formatter
        val formatter =
            DateTimeFormatter.ofPattern("yyyy.MM.dd")
                .withResolverStyle(ResolverStyle.STRICT)

        return dateRegex.findAll(text) // 정규식에 해당되는 모든 부분 찾기
            .mapNotNull {
                // 모든 종류의 공백 제거 -> . or - 으로 split
                val parts = it.value.replace(Regex("\\s+"), "").split('.', '-')

                // 연, 월, 일 변수에 각각 저장
                var year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()

                // 2자리 연도 -> 4자리 연도로 변환
                if (year < 100) year += 2000

                try {
                    // LocalDate로 유효성 검사 후 formatter로 변환
                    val date = LocalDate.of(year, month, day)
                    date.format(formatter)
                } catch (e: Exception) {
                    // 변환 실패 시, null 반환
                    null
                }
            }.toList() // 리스트로 최종 결과 반환
    }

    override fun onDestroyView() {
        super.onDestroyView()

        cameraProvider?.unbindAll()
        cameraExecutor.shutdownNow()
        imageAnalyzer.clearAnalyzer()
        _binding = null
    }

    // onnx + 라벨링 txt 파일 불러오기, OrtSession 객체 생성
    private fun load() {
        // 파일 불러오기
        dataProcess.loadModel()
        dataProcess.loadLabel()

        // OrtSession 객체 생성
        ortEnvironment = OrtEnvironment.getEnvironment()
        session =
            ortEnvironment.createSession(
                this.context?.filesDir?.absolutePath.toString() + "/" + DataProcess.FILE_NAME,
                OrtSession.SessionOptions(),
            )

        // assets의 txt 파일을 불러와서 RectView에 라벨 클래스 전달
        binding.rectView.setClassLabel(dataProcess.classes)
    }
}
