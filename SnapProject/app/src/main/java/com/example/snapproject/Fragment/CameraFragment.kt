package com.example.snapproject.Fragment

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.snapproject.DataProcess
import com.example.snapproject.MainActivity
import com.example.snapproject.api.ApiRepository
import com.example.snapproject.api.ApiResult
import com.example.snapproject.databinding.FragmentCameraBinding
import com.example.snapproject.navigateSafe
import com.example.snapproject.readText
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
    private var uriArrayList: ArrayList<String> = arrayListOf() // 이미지 파일 저장 경로 ArrayList

    private lateinit var dataProcess: DataProcess

    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions
    private var isNameDetected: Boolean = false
    private var isRequesting = false // 현재 POST 요청 중인지 여부를 알려주는 상태 변수

    // TextRecognizer 인스턴스 생성
    val txtRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    // OrtSession 관련 변수
    private lateinit var ortEnvironment: OrtEnvironment
    private lateinit var session: OrtSession

    // YOLO 추론 후, OCR 결과를 저장할 변수
    private var productName: String? = null // 제품명 OCR 결과
    private var expirationDate: String? = null // 소비기한 OCR 결과

    // TTS로 안내한 횟수를 저장할 변수
    private var productNameTTSNum: Int = 0 // 제품명 TTS 횟수
    private var expirationDateTTSNum: Int = 0 // 소비기한 TTS 횟수
    private var productLabelTTSNum: Int = 0 // 제품 라벨 TTS 횟수

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
                MainActivity.tts.readText("카메라 권한을 허용해야 앱 사용이 가능합니다.") {
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
                    MainActivity.tts.readText("앱 설정에서 카메라 권한을 허용해주세요.")
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData("package:${mContext.packageName}".toUri())
                    settingPermissionLauncher.launch(intent)
                } else { // 사용자가 한 번만 거부한 경우
                    MainActivity.tts.readText("카메라 권한이 필요합니다.") {
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

        // 버튼 클릭 이벤트 처리 코드를 여기에 추가해야(initView 함수 안이 X) onResume된 후에도 해당 코드가 정상 작동함.
        binding.btnComplete.setOnClickListener {
            val action = CameraFragmentDirections.actionCameraFragmentToLoadingFragment(uriArrLst = uriArrayList.toTypedArray())
            findNavController().navigate(action)
        }

        binding.btnCapture.setOnClickListener { // 하단의 원형 버튼 클릭 시
            takePhoto() // 사진 촬영 및 이미지 파일 저장
        }
    }

    // 시스템 설정에서 권한 허용해 준 뒤, 다시 돌아왔을 때 카메라 세팅 필요
    override fun onResume() {
        super.onResume()
        if (hasPermissions(mContext)) {
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
        val imageAnalyzer =
            ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor()) {
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

    // 카메라 캡쳐 및 이미지 파일 Cache 디렉터리에 저장
    private fun takePhoto() {
        val mImageCapture = imageCapture ?: return

        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA).format(System.currentTimeMillis()) // 파일명 설정
        val imgFile = File(requireContext().cacheDir, "$fileName.png") // File 객체 (캐시 directory에 저장)

        // 캡쳐 이미지 -> 이미지 파일 변경 시, 사용할 옵션 설정 (저장 위치 등)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(imgFile).build()

        // 사진 촬영
        mImageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                // 이미지 캡쳐 및 저장 실패
                override fun onError(exc: ImageCaptureException) {
                    Log.d("CameraFragment", "촬영 실패 : ${exc.message}", exc)
                }

                // 이미지 캡쳐 및 저장 성공
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let { uriArrayList.add(it.toString()) } // 이미지 저장 경로를 ArrayList에 추가

                    Log.d("CameraFragment", "저장된 파일 경로 : ${outputFileResults.savedUri}") // 이미지 저장 경로 확인
                }
            },
        )
    }

    // 이미지 처리 함수
    private fun imageProcess(imageProxy: ImageProxy) {
        val b = binding ?: return // 화면 전환 시, NullPointer 에러 방지를 위해 b 변수를 대신 사용

        // TTS 발화 횟수 3회 이상이면, 다음 화면으로 이동
        mActivity.runOnUiThread { // IllegalStateException 에러 방지 - UI 작업은 메인 스레드에서 수행
            if (productNameTTSNum >= 3 && expirationDateTTSNum >= 3 && productLabelTTSNum >= 3) {
                val action =
                    CameraFragmentDirections.actionCameraFragmentToLoadingFragment(uriArrLst = uriArrayList.toTypedArray())
                findNavController().navigateSafe(resId = action.actionId, args = action.arguments)
            }
        }

        load() // onnx + 라벨링 txt 파일 불러오기, OrtSession 객체 생성

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
        b.rectView.transformRect(results, b.previewCamera.width, b.previewCamera.height) // 실제 기기 화면 크기에 맞게 좌표값 조정
        b.rectView.invalidate() // 최종 결과를 화면에 그려줌

        // 화면에 그려진 Rect 크기만큼 비트맵 이미지 생성
        val drawRect = b.rectView.getDrawRect() // 화면에 그려진 Rect 가져오기
        val fullBitmap = imageProxy.toBitmap() // 전체 Preview에 대한 비트맵 이미지 생성

        // drawRect를 카메라 Bitmap 크기에 맞게 변환해줄 때 필요한 변수
        val scaleX = fullBitmap.width.toFloat() / b.previewCamera.width
        val scaleY = fullBitmap.height.toFloat() / b.previewCamera.height

        if (drawRect != null) { // drawRect가 화면에 표시된 상태라면
            // drawRect에 Scale 값을 곱해서 카메라 Bitmap 크기에 맞게 변환
            val left = (drawRect.left * scaleX).toInt()
            val top = (drawRect.top * scaleY).toInt()
            val width = ((drawRect.right - drawRect.left) * scaleX).toInt()
            val height = ((drawRect.bottom - drawRect.top) * scaleY).toInt()

            Log.d("croppedImg", "left: $left, top: $top, width: $width, height: $height")
            Log.d("bitmapImg", "${fullBitmap.width}, ${fullBitmap.height}")

            // 제품명을 1번만 detect하도록 설정 + 중복 요청 방지
            if (width > 0 && height > 0 && results.firstOrNull()?.classIndex == 1 && !isRequesting && !isNameDetected) {
                isRequesting = true // 중복 요청 방지를 위한 변수 (현재 POST 요청 중)

                // RectView (YOLO 추론 결과 그림) 크기만큼 bitmap 이미지 생성
                val croppedBitmap = Bitmap.createBitmap(fullBitmap, left, top, width, height)
                Log.d("croppedBitmap", "$croppedBitmap")

                // 비트맵 이미지를 File(.png)로 저장
                val imgFile = saveBitmapToFile(fullBitmap)

                // 인식한 제품명 이미지를 서버로 전송하여 OCR 요청, 응답 결과 표시
                lifecycleScope.launch {
                    when (val result = ApiRepository.postName(imgFile)) { // POST 요청
                        is ApiResult.Success -> { // 성공한 경우 -> Log로 인식된 제품명 출력
                            takePhoto() // 사진 촬영 및 이미지 파일 저장
                            Log.d("postNameResult", result.data.productName)
                            MainActivity.tts.readText(result.data.productName) // 제품명 TTS 출력
                            productNameTTSNum++ // 제품명 TTS 횟수 증가
                            productName = result.data.productName // 제품명 인식 결과 저장
                            isNameDetected = true // 제품명이 인식되었으므로, true로 상태 변경
                        }
                        is ApiResult.Error -> { // 실패한 경우
                            null
                        }
                    }
                    isRequesting = false
                }
            }

            // 제품명 TTS 출력
            if (results.firstOrNull()?.classIndex == 1 && productNameTTSNum < 3 && isNameDetected) {
                // 조건 : 제품명이 인식됨 + 제품명 TTS 횟수가 3 미만 + 제품명 OCR POST 요청 성공
                takePhoto() // 사진 촬영 및 이미지 파일 저장
                productName?.let { MainActivity.tts.readText(it) } // 제품명 TTS 출력
                productNameTTSNum++ // 제품명 TTS 횟수 증가
            }

            // "제품 라벨 인식됨" -> TTS 출력
            if (results.firstOrNull()?.classIndex == 0 && productLabelTTSNum < 3) { // 조건 : 제품 라벨이 인식됨 + 제품 라벨 TTS 횟수가 3 미만
                takePhoto() // 사진 촬영 및 이미지 파일 저장
                MainActivity.tts.readText("제품 라벨이 인식되었습니다.") // "제품 라벨 인식됨" -> TTS 출력
                productLabelTTSNum++ // 제품 라벨 TTS 횟수 증가
            }
        }

        // 소비기한 OCR 수행
        recognizeExpiryDate(fullBitmap)
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
                    expiryDateTTS() // 인식된 소비기한 TTS 출력
                } else { // 소비기한이 인식되지 않은 경우
                    Log.d("ocrDateEmpty", "소비기한이 인식되지 않음")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ocrDateError", "${e.message}")
            }
    }

    // 인식된 소비기한 TTS 출력
    private fun expiryDateTTS() {
        if (expirationDateTTSNum < 3) { // TTS로 음성 안내한 횟수가 3회 미만인지 체크
            takePhoto() // 사진 촬영 및 이미지 파일 저장
            expirationDate?.let { MainActivity.tts.readText(it) } // 인식된 소비기한 TTS 출력
            expirationDateTTSNum++ // TTS 횟수 1씩 증가
            Log.d("expirationDateTTSNum", "$expirationDateTTSNum")
        } else { // TTS로 음성 안내한 횟수가 10회라면 -> TTS 출력하지 않고 바로 리턴
            return
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
                // 공백 제거 -> . or - 으로 split
                val parts = it.value.replace(" ", "").split('.', '-')

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

    override fun onDestroy() {
        super.onDestroy()
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
