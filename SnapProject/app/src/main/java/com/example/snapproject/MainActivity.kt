package com.example.snapproject

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.snapproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnChildButtonClickListener {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var navController: NavController

    // 자식 프래그먼트의 버튼 클릭 리스너 구현
    override fun onChildButtonClicked(destinationId: Int) {
        // 자식 프래그먼트로부터 전달받은 이벤트 수행 (화면 전환)
        findNavController(R.id.nav_host_fragment).navigate(destinationId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰 바인딩
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        setUpJetpackNavigation() // 화면 전환 컨트롤러 -> 프래그먼트 전환

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Navigation Controller (화면 전환 컨트롤러)를 통해 프래그먼트 전환 수행
    private fun setUpJetpackNavigation() {
        val host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
    }

    // 키보드 보여주는 함수
    fun showSoftInput(view: View) {
        view.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    // 키보드 숨기는 함수 (키보드 바깥쪽 터치했을 때)
    fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        if (currentFocus is EditText) currentFocus?.clearFocus() // 현재 Focus된 게 EditText라면, Focus 제거하기
    }
}
