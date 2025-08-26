package com.example.snapproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.snapproject.databinding.ActivityMainBinding
import java.io.File
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private var selectedImgFile: List<File>? = null

    // 결과 콜백을 통해 사용자가 선택한 파일의 데이터 가져오기(ActivityResult 받기)
    private var filePickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) { // 사용자가 파일을 선택 (선택 파일 데이터 가져오는 작업 성공)
                val clipData = result.data?.clipData
                val singleUri = result.data?.data
                val files = mutableListOf<File>()

                clipData?.let { cd ->
                    for (i in 0 until cd.itemCount) {
                        val uri = cd.getItemAt(i).uri
                        val name = getFileName(this@MainActivity, uri)
                        files.add(uriToFile(uri, name))
                    }
                } ?: singleUri?.let { uri ->
                    val name = getFileName(this@MainActivity, uri)
                    files.add(uriToFile(uri, name))
                }

                if (files.isNotEmpty()) {
                    selectedImgFile = files
                    Toast.makeText(this, "${files.size}개의 이미지를 선택했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mainBinding.btnImg.setOnClickListener {
            showFileChooser()
        }

        mainBinding.btnSend.setOnClickListener {
            tryPostAnalyze()
        }

    }

    // 스크립트 파일명 가져오는 함수
    private fun getFileName(
        context: Context,
        uri: Uri,
    ): String {
        var fileName = "" // 파일명 변수 초기화

        // uri를 query문에 담아서 넘겨줌 -> query 실행 결과가 커서 객체 형태로 반환됨.
        // cursor : 파일 정보가 담긴 결과 목록
        val fileCursor = context.contentResolver.query(uri, null, null, null, null)
        fileCursor.use { // cursor 객체가 레코트를 한 줄씩 읽음.
            if (fileCursor!!.moveToFirst()) { // 첫 번째 행(선택한 파일)으로 이동
                val nameIndex =
                    fileCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) // 파일명을 가리키는 컬럼 인덱스
                if (nameIndex != -1) { // 파일명을 찾았다면
                    fileName = it!!.getString(nameIndex) // 파일명 가져와서 변수에 저장
                }
            }
        }
        return fileName // 찾아낸 파일명 반환
    }

    // 문서 선택기 여는 함수
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT) // 문서 파일에서 데이터 가져오기
        intent.type = "image/*" // 인텐트가 가져올 타입 지정 (txt 파일만 필터링)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 개 선택 가능
        intent.addCategory(Intent.CATEGORY_OPENABLE) // open 가능한 파일들 카테고리화

        try {
            // 사용자 파일 선택 -> 결과를 filePickerLauncher가 처리
            filePickerLauncher.launch(Intent.createChooser(intent, "파일 선택"))
        } catch (exception: Exception) {
            Toast.makeText(this, "문서 선택기 open 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 선택한 스크립트 -> File 타입으로 리턴
    private fun uriToFile(
        uri: Uri,
        fileName: String,
    ): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(cacheDir, fileName)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file
    }

    private fun tryPostAnalyze() {
        val uuid = "user123"
        selectedImgFile?.let { file ->
            lifecycleScope.launch {
                when (val result = uuid.let { ApiRepository.postAnalyze(it, file) }) {
                    is ApiResult.Success -> { // 서버 통신 성공한 경우
                        mainBinding.tvResp.text = result.data.toString()
                    }
                    is ApiResult.Error -> { // 서버 통신 실패한 경우
                        Toast.makeText(this@MainActivity, "업로드 실패!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: Toast.makeText(this, "이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
    }
}
