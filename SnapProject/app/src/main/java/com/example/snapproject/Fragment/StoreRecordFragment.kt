package com.example.snapproject.Fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.snapproject.MainActivity
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentStoreRecordBinding
import com.example.snapproject.model.db.Product
import com.example.snapproject.model.db.ProductDatabase
import com.example.snapproject.readText

class StoreRecordFragment : Fragment() {
    private var _binding: FragmentStoreRecordBinding? = null
    private val binding get() = _binding!!

    // Context, Activity 변수
    private lateinit var mContext: Context
    private lateinit var mActivity: MainActivity

    // SpeechRecognizer 관련 변수
    private lateinit var recogIntent: Intent
    private lateinit var mRecognizer: SpeechRecognizer

    private lateinit var storageLocation: String // 사용자가 입력한 제품 보관 장소

    companion object {
        fun newInstance() = StoreRecordFragment()

        // 필요한 권한 array 선언 및 초기화 (오디오 녹음)
        private val PERMISSIONS_REQUIRED =
            arrayOf(android.Manifest.permission.RECORD_AUDIO)
    }

    // 앱 설정 Permission 콜백 등록 (앱 설정에서의 사용자 이벤트 처리)
    private val settingPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!hasPermissions(mContext)) { // 사용자가 앱 설정에서도 권한 허용을 해주지 않은 경우
                MainActivity.tts.readText("오디오 녹음 권한을 허용해야 앱 사용이 가능합니다.") {
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
                    MainActivity.tts.readText("앱 설정에서 오디오 녹음 권한을 허용해주세요.") {
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData("package:${mContext.packageName}".toUri())
                        settingPermissionLauncher.launch(intent)
                    }
                } else { // 사용자가 한 번만 거부한 경우
                    MainActivity.tts.readText("오디오 녹음 권한이 필요합니다.") {
                        findNavController().popBackStack() // 제품 상세 설명 화면으로 이동
                    }
                }
            }
        }

    override fun onResume() {
        super.onResume()
        view?.post { // view가 생성된 후 실행
            binding.storeLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS // 기존 Talkback focus 지우기

            // TTS 발화 먼저 진행 -> 발화 끝난 뒤, 다시 Talkback focus 복원
            MainActivity.tts.readText("화면 중앙의 음성 녹음 버튼을 눌러, 제품 보관 장소를 음성으로 입력해주세요.") {
                binding.storeLayout.post { binding.storeLayout.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStoreRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    // context와 activity 가져오는 함수
    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
        mActivity = context as MainActivity
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 제품 상세 설명 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnNext.setOnClickListener { // 다음으로 버튼 클릭 -> DB의 테이블 Update 로직 추가 필요
            // Safe Args로 받은 데이터 가져오기
            val args: StoreRecordFragmentArgs by navArgs()
            val prevPage = args.prevPage
            val response = args.analyzeResponse

            if (prevPage == "loading") { // 로딩 화면에서 넘어온 경우 -> 서버 응답 결과 가져와서 테이블에 Insert
                // Safe Args로 받은 서버 응답 결과 -> 각각 변수에 저장
                val productName = response?.data?.productName
                val expirationDate = response?.data?.expirationDate
                val summary = response?.data?.summary
                val ingredients = response?.data?.ingredients

                if (this::storageLocation.isInitialized && productName != null &&
                    expirationDate != null && summary != null && ingredients != null
                ) { // 누락된 정보가 없는 경우
                    insertStorage(storageLocation, productName, expirationDate, summary, ingredients) // 테이블에 신규 제품 Insert
                    findNavController().popBackStack() // 제품 상세 설명 화면으로 이동
                } else { // 누락된 정보가 있다면
                    MainActivity.tts.readText("제품 정보를 저장할 수 없습니다!")
                }
            }
        }

        binding.btnKeyBoard.setOnClickListener { // "키보드로 입력" 버튼 클릭
            binding.edtTxtStore.isEnabled = true // EditText 수정 가능
            binding.edtTxtStore.setText("") // 기존에 입력해둔 내용 지우기
            binding.edtTxtStore.hint = "보관 장소를\n입력해주세요."
            mActivity.showSoftInput(binding.edtTxtStore) // MainActivity의 키보드 보여주는 함수 호출
        }

        // 키보드 바깥쪽 레이아웃 클릭 이벤트
        binding.storeLayout.setOnTouchListener { _, _ ->
            mActivity.hideKeyboard(binding.edtTxtStore) // 키보드 숨기기
            binding.edtTxtStore.isEnabled = false // EditText 수정 및 클릭 불가
            false
        }

        // 음성 녹음 터치 이벤트 - 버튼을 누르기 시작했을 때, 버튼을 눌렀다가 떼었을 때
        binding.btnRecord.setOnClickListener {
            binding.edtTxtStore.hint = "" // "키보드 입력 시도 -> 음성 인식 시도"하는 경우를 고려해서 추가한 코드
            binding.edtTxtStore.setText("") // 기존에 입력해둔 내용 지우기

            // RecognizerIntent 생성
            recogIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            recogIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.packageName)
            recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

            // Speech-To-Text 시작
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext)
            mRecognizer.setRecognitionListener(listener)
            mRecognizer.startListening(recogIntent)
        }
    }

    // Product 테이블에 새로운 제품을 Insert하는 함수
    private fun insertStorage(
        location: String,
        name: String,
        date: String,
        summary: List<String>,
        ingredients: String,
    ) {
        val productDB = ProductDatabase.getInstance(requireContext())
        val product = Product(location, name, date, summary, ingredients)
        productDB?.productDao()?.insert(product)
    }

    // SpeechRecognizer 관련 리스너 설정
    private val listener: RecognitionListener =
        object : RecognitionListener {
            // 말하기 준비 되었을 때 (위의 터치 리스너 ACTION_DOWN - 버튼을 누르기 시작한 이후에 동작)
            override fun onReadyForSpeech(params: Bundle?) {
                binding.edtTxtStore.hint = "이제 말해주세요"
            }

            // 음성 녹음 시작 시
            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(rmsdB: Float) {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            // 말하기를 끝냈을 때
            override fun onEndOfSpeech() {
            }

            // 에러 발생 시
            override fun onError(error: Int) {
                binding.edtTxtStore.hint = "음성 인식 오류.\n다시 시도해주세요."
                MainActivity.tts.readText("음성 인식 오류 발생. 다시 시도해주세요.")
            }

            // 음성 인식 종료
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                for (i in matches!!.indices) binding.edtTxtStore.setText('"' + matches[i] + '"') // TextView에 음성 인식 결과 반영
                storageLocation = matches[0] // 입력한 보관 장소 -> 별도의 변수에 저장
                MainActivity.tts.readText("음성 인식 결과는 ${storageLocation}입니다.")
            }

            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(
                eventType: Int,
                params: Bundle?,
            ) {
            }
        }

    private fun initView() =
        with(binding) {
            // 필요한 권한이 모두 허용된 상태가 아니라면 -> 권한 요청 Dialog 띄우기
            if (!hasPermissions(mContext)) {
                requestPermissionLauncher.launch(PERMISSIONS_REQUIRED)
            }
            edtTxtStore.isEnabled = false // EditText 수정 및 클릭 불가
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
