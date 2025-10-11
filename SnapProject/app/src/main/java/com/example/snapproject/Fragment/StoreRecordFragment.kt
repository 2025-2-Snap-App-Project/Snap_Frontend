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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.snapproject.MainActivity
import com.example.snapproject.databinding.FragmentStoreRecordBinding

class StoreRecordFragment : Fragment() {
    private var _binding: FragmentStoreRecordBinding? = null
    private val binding get() = _binding!!

    // Context, Activity 변수
    private lateinit var mContext: Context
    private lateinit var mActivity: MainActivity

    // SpeechRecognizer 관련 변수
    private lateinit var recogIntent: Intent
    private lateinit var mRecognizer: SpeechRecognizer

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
                Toast.makeText(mContext, "오디오 녹음 권한을 허용해야 앱 사용이 가능합니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // 홈 화면 이동
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
                    Toast.makeText(mContext, "앱 설정에서 오디오 녹음 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData("package:${mContext.packageName}".toUri())
                    settingPermissionLauncher.launch(intent)
                } else { // 사용자가 한 번만 거부한 경우
                    Toast.makeText(mContext, "오디오 녹음 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // 제품 상세 설명 화면으로 이동
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

        // Safe Args로 받은 데이터 가져오기
        val args: StoreRecordFragmentArgs by navArgs()
        val prevPage = args.prevPage

        initView()

        binding.btnBack.setOnClickListener { // 이전 버튼 클릭 -> 제품 상세 설명 화면으로 이동
            findNavController().popBackStack()
        }
        binding.btnNext.setOnClickListener { // 다음으로 버튼 클릭 -> DB의 테이블 Update 로직 추가 필요
            findNavController().popBackStack() // 제품 상세 설명 화면으로 이동
        }

        // 음성 녹음 터치 이벤트 - 버튼을 누르기 시작했을 때, 버튼을 눌렀다가 떼었을 때
        binding.btnRecord.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { // 버튼을 누르기 시작했을 때 -> Speech-To-Text 시작
                    // RecognizerIntent 생성
                    recogIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    recogIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.packageName)
                    recogIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

                    // Speech-To-Text 시작
                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext)
                    mRecognizer.setRecognitionListener(listener)
                    mRecognizer.startListening(recogIntent)
                }
                MotionEvent.ACTION_UP -> { // 버튼을 눌렀다가 떼었을 때 -> Speech-To-Text 종료
                    listener.onEndOfSpeech()
                }
            }
            true
        }
    }

    // SpeechRecognizer 관련 리스너 설정
    private val listener: RecognitionListener =
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
            }

            // 음성 녹음 시작 시
            override fun onBeginningOfSpeech() {
                binding.tvStore.text = "듣고 있습니다..."
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
                binding.tvStore.text = "음성 인식 오류.\n다시 시도해주세요."
            }

            // 음성 인식 종료
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                for (i in matches!!.indices) binding.tvStore.text = '"' + matches[i] + '"' // TextView에 음성 인식 결과 반영
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
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
