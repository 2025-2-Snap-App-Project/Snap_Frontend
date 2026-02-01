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
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.snapproject.MainActivity
import com.example.snapproject.R
import com.example.snapproject.api.ApiRepository
import com.example.snapproject.api.ApiResult
import com.example.snapproject.databinding.FragmentCameraBinding
import com.example.snapproject.readText
import com.example.snapproject.viewmodel.CameraViewModel
import com.example.snapproject.yolo.DataProcess
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by activityViewModels() // CameraViewModel 공유

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

    // TextRecognizer 인스턴스 생성
    val txtRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    // OrtSession 관련 변수
    private lateinit var ortEnvironment: OrtEnvironment
    private lateinit var session: OrtSession

    private var productLabelTxt: String = "제품 라벨이 인식되었습니다."

    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun newInstance() = CameraFragment()

        // 필요한 권한 array 선언 및 초기화 (카메라 촬영)
        private val PERMISSIONS_REQUIRED =
            arrayOf(android.Manifest.permission.CAMERA)

        // 소비기한 OCR 확정을 위한 상수
        const val DATE_BUFFER_SIZE = 6 // 버퍼 사이즈 (6개 프레임만 확인)
        const val DATE_CONFIRM_COUNT = 3 // 확정 기준 (해당 날짜가 3번 이상 나오면 확정)
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
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)
        return binding.root
    }

    // context와 activity 가져오는 함수
    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
        mActivity = context as MainActivity

        // mContext 초기화된 뒤에, DataProcess 객체 생성
        viewModel.dataProcess = DataProcess(context = mContext)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        // YOLO 객체 탐지가 성공한 경우
        viewModel.yoloResults.observe(viewLifecycleOwner) { results ->
            // 비어있으면 리턴
            if (results.isEmpty()) return@observe
            val firstResult = results.firstOrNull() ?: return@observe

            // 프레임의 중앙에 객체가 위치해있는지 / 화면 끝에 걸쳐져있는지 체크
            if (isRectOnEdge(firstResult.rectF, viewModel.yoloBitmap)) {
                Log.d("isRectOnEdge", "YOLO 추론한 Rect가 화면 끝에 걸쳐진 상태 : $firstResult")
                return@observe // 화면 끝에 걸쳐져 있는 것이므로 바로 리턴
            }
            Log.d("isRectOnEdge", "YOLO 추론한 Rect가 화면 중앙에 위치 : $firstResult")

            val croppedBitmap = cropBitmapWithRect(viewModel.yoloBitmap, firstResult.rectF)

            // 위에서 isRectOnEdge(), cropBitmapWithRect()를 모두 호출한 뒤에, RectF 좌표를 변환해줘야 함 (순서 주의)
            // RectF 좌표 변환 후, RectView 그리기 (YOLO Bounding Box)
            binding.rectView.run {
                transformRect(results)
                invalidate()
            }

            // "제품명 인식됨" -> 서버로 전송하여 OCR 요청 -> 응답 결과 TTS 출력
            if (firstResult.classIndex == 1 && !viewModel.isRequesting) {
                viewModel.isRequesting = true
                val imgFile = viewModel.saveBitmapToFile(viewModel.yoloBitmap, "name", requireContext())

                lifecycleScope.launch {
                    when (val result = ApiRepository.postName(imgFile)) { // POST 요청
                        is ApiResult.Success -> { // 성공한 경우 -> Log로 인식된 제품명 출력
                            val productName = result.data.productName // 제품명 인식 결과 저장
                            viewModel.onProductNameDetected(productName, viewModel.yoloBitmap)
                        }
                        is ApiResult.Error -> {
                            Log.e("productNameTTS", "서버 요청 실패")
                            viewModel.isRequesting = false
                        }
                    }
                }
            }

            // "제품 라벨 인식됨" -> TTS 출력
            if (firstResult.classIndex == 0) {
                viewModel.onProductLabelDetected(viewModel.yoloBitmap)
            }
        }

        // 제품명 인식되면 실행
        viewModel.productName.observe(viewLifecycleOwner) { name ->
            if (name == null) return@observe
            // TTS 출력
            MainActivity.tts.readText(name, requireContext()) {
                viewModel.onNameTTSCompleted()
                checkAllTTSCompleted()
            }
        }

        // 소비기한 인식되면 실행
        viewModel.expirationDate.observe(viewLifecycleOwner) { date ->
            if (date == null) return@observe

            // TTS 출력
            MainActivity.tts.readText(date, requireContext()) {
                viewModel.onDateTTSCompleted()
                checkAllTTSCompleted()
            }
        }

        // 제품 라벨 인식되면 실행
        viewModel.productLabel.observe(viewLifecycleOwner) { label ->
            if (label == null) return@observe

            // TTS 출력
            MainActivity.tts.readText("제품 라벨이 인식되었습니다.", requireContext()) {
                viewModel.onLabelTTSCompleted()
                checkAllTTSCompleted()
            }
        }
    }

    // YOLO 객체 bounding box가 가장자리에 위치해있는지 체크
    // (left, top, right, bottom 중 하나라도 가장자리에 위치해 있으면 true, 나머지는 전부 false)
    private fun isRectOnEdge(
        rectF: RectF,
        fullBitmap: Bitmap,
    ): Boolean {
        val width = rectF.width()
        val height = rectF.height()
        val bitmapWidth = fullBitmap.width
        val bitmapHeight = fullBitmap.height

        if (width == 0f || height == 0f) return true // 높이와 너비 둘 중 하나가 0이면 true

        // 화면의 5%
        val marginX = width * 0.05f
        val marginY = height * 0.05f

        return rectF.left <= marginX ||
            rectF.top <= marginY ||
            rectF.right >= bitmapWidth - marginX ||
            rectF.bottom >= bitmapHeight - marginY
    }

    // YOLO의 bounding box 크기만큼 crop해서 비트맵 생성
    private fun cropBitmapWithRect(
        source: Bitmap,
        rectF: RectF,
    ): Bitmap {
        // bounding box(rectF) 좌표
        val left = rectF.left.coerceIn(0f, source.width.toFloat()).toInt()
        val top = rectF.top.coerceIn(0f, source.height.toFloat()).toInt()
        val right = rectF.right.coerceIn(0f, source.width.toFloat()).toInt()
        val bottom = rectF.bottom.coerceIn(0f, source.height.toFloat()).toInt()

        // bounding box(rectF)의 너비와 높이 계산
        val width = right - left
        val height = bottom - top

        // 너비와 높이가 0 이하일 때 에러 처리
        if (width <= 0 || height <= 0) {
            throw IllegalArgumentException("Invalid crop rect: $rectF")
        }

        // bounding box(rectF) 크기만큼 비트맵 새로 생성
        return Bitmap.createBitmap(
            source,
            left,
            top,
            width,
            height,
        )
    }

    // 3가지 모두 인식되었는지 체크
    private fun checkAllTTSCompleted() {
        Log.d(
            "CameraFragment",
            "name=${viewModel.isNameTTSCompleted}, " +
                "date=${viewModel.isDateTTSCompleted}, " +
                "label=${viewModel.isLabelTTSCompleted}",
        )
        if (viewModel.isNameTTSCompleted && viewModel.isDateTTSCompleted && viewModel.isLabelTTSCompleted) {
            Log.d("CameraFragment", "ALL DONE → NAVIGATE")

            requireActivity().runOnUiThread {
                val action =
                    CameraFragmentDirections.actionCameraFragmentToLoadingFragment(uriArrLst = uriArrayList.toTypedArray())
                findNavController().navigate(action)
            }
        }
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

        // 카메라 4:3 비율로 고정
        val resolutionSelector =
            ResolutionSelector.Builder()
                .setAspectRatioStrategy(
                    AspectRatioStrategy(
                        AspectRatio.RATIO_4_3,
                        AspectRatioStrategy.FALLBACK_RULE_AUTO,
                    ),
                )
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(1280, 960),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER,
                    ),
                )
                .build()

        // 카메라 Preview 설정
        preview =
            Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.surfaceProvider = binding.previewCamera.surfaceProvider
                }

        // 이미지 캡쳐 Builder 객체 생성
        imageCapture = ImageCapture.Builder().build()

        // 이미지 분석을 위한 ImageAnalysis 객체 생성 및 세팅
        imageAnalyzer =
            ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

        imageAnalyzer.setAnalyzer(cameraExecutor) {
            if (!isAdded || view == null || _binding == null) { // Fragment가 attach된 상태가 아니거나, view가 null이거나, _binding이 null이면
                // imageProxy를 닫고 바로 리턴
                it.close()
                return@setAnalyzer
            }
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

    // 저장된 이미지 파일 경로를 ArrayList에 추가
    private fun addUriArrayList(uri: Uri) {
        uriArrayList.add(uri.toString())
    }

    // 이미지 처리 함수
    private fun imageProcess(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap() // 원본 비트맵
        val yoloBitmap = bitmap.scale(DataProcess.INPUT_SIZE, DataProcess.INPUT_SIZE) // YOLO 입력 비트맵

        // 한 프레임에 YOLO/ML-Kit 둘 중에 하나만 실행
        if (viewModel.runYOLO) { // 현재 프레임은 YOLO만 실행하는 프레임이다
            val floatBuffer = viewModel.dataProcess.bitmapToFloatBuffer(yoloBitmap)
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
            val results = viewModel.dataProcess.outputsToNPMSPredictions(outputs) // YOLO 추론 최종 결과를 result에 저장
            viewModel.onYoloResult(results, yoloBitmap) // YOLO 추론 결과 업데이트
        } else { // 현재 프레임은 OCR만 실행하는 프레임이다
            // 소비기한 OCR 시작
            val image = InputImage.fromBitmap(bitmap, imageProxy.imageInfo.rotationDegrees) // Bitmap에서 InputImage 생성

            // OCR 수행
            txtRecognizer.process(image)
                .addOnSuccessListener { // OCR 성공
                    if (viewModel.isDateDetected.value == true) return@addOnSuccessListener // 소비기한이 확정되었다면, 바로 리턴
                    val dates = extractValidDates(it.text) // 소비기한 조건 체크
                    if (dates.isEmpty()) return@addOnSuccessListener // 빈 리스트인 경우, 바로 리턴

                    // 인식된 날짜 출력
                    val ocrDate = dates.first()
                    Log.d("ocrDateSuccess", "인식된 날짜: $ocrDate")


                    viewModel.onExpirationDateDetected(ocrDate)
                }
                .addOnFailureListener { e -> // OCR 실패
                    Log.e("ocrDateError", "${e.message}")
                }
        }

        // 다음 프레임에는 반대 작업 수행 (지금 YOLO를 실행했다면, 다음 프레임은 ML-Kit 실행한다. 반대의 경우도 마찬가지)
        viewModel.runYOLO = !viewModel.runYOLO
    }

    // 날짜 후보를 버퍼에 저장 -> 가장 많이 나온 날짜 선택
    private fun voteExpirationDate(candidate: String): String? {
        // 날짜 후보를 버퍼에 추가
        viewModel.dateBuffer.add(candidate)

        // 가장 최근 N개(DATE_BUFFER_SIZE) 프레임만 확인
        if (viewModel.dateBuffer.size > DATE_BUFFER_SIZE) {
            viewModel.dateBuffer.removeAt(0)
        }

        // 후보 날짜별 등장 횟수 계산
        val countMap = mutableMapOf<String, Int>()
        for (date in viewModel.dateBuffer) {
            countMap[date] = (countMap[date] ?: 0) + 1
        }

        // (가장 많이 나온 날짜, 등장 횟수) 저장
        var mostVotedDate: String? = null // 가장 많이 나온 날짜
        var maxCount = 0 // 등장 횟수
        for ((date, count) in countMap) {
            if (count > maxCount) {
                mostVotedDate = date
                maxCount = count
            }
        }

        // 등장 횟수가 기준값(DATE_CONFIRM_COUNT) 이상이면 소비기한 확정
        if (mostVotedDate != null && maxCount >= DATE_CONFIRM_COUNT) {
            viewModel.dateBuffer.clear() // 소비기한 확정 후 버퍼 초기화
            return mostVotedDate // 확정된 소비기한 String 반환
        }

        // 아직 7을 넘지 못했다면 null 반환
        return null
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
        imageAnalyzer.clearAnalyzer()
        cameraExecutor.shutdownNow()
        _binding = null
    }

    // onnx + 라벨링 txt 파일 불러오기, OrtSession 객체 생성
    private fun load() {
        // 파일 불러오기
        viewModel.dataProcess.loadModel()
        viewModel.dataProcess.loadLabel()

        // OrtSession 객체 생성
        ortEnvironment = OrtEnvironment.getEnvironment()
        session =
            ortEnvironment.createSession(
                this.context?.filesDir?.absolutePath.toString() + "/" + DataProcess.FILE_NAME,
                OrtSession.SessionOptions(),
            )

        // assets의 txt 파일을 불러와서 RectView에 라벨 클래스 전달
        binding.rectView.setClassLabel(viewModel.dataProcess.classes)
    }
}
