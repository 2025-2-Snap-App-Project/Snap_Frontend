package com.example.snapproject.Fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.snapproject.MainActivity
import com.example.snapproject.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var mActivity: MainActivity

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
                Toast.makeText(mContext, "카메라 권한을 허용해야 앱 사용이 가능합니다.", Toast.LENGTH_SHORT).show()

                // 홈 화면으로 이동하는 로직 추가하기
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
                            mContext,
                            permission,
                        ) == PackageManager.PERMISSION_DENIED &&
                                !ActivityCompat.shouldShowRequestPermissionRationale(
                                    mActivity,
                                    permission,
                                )
                    }
                if (noAskAgain) { // 사용자가 다시 묻지 않음을 선택한 경우 -> 앱 설정 화면으로 이동
                    Toast.makeText(mContext, "앱 설정에서 카메라 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData("package:${mContext.packageName}".toUri())
                    settingPermissionLauncher.launch(intent)
                } else { // 사용자가 한 번만 거부한 경우
                    Toast.makeText(mContext, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()

                    // 홈 화면으로 이동하는 로직 추가하기
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
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() = with(binding) {
        // 2개의 권한이 모두 허용된 상태가 아니라면 -> 권한 요청 Dialog 띄우기
        if (!hasPermissions(mContext)) {
            requestPermissionLauncher.launch(PERMISSIONS_REQUIRED)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
