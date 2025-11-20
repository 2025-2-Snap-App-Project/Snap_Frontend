package com.example.snapproject.api

import com.example.snapproject.model.AnalyzeResponse
import com.example.snapproject.model.NameResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/*
*
*   1) 인터페이스에 선언된 메서드를 apiSafeCall의 인자로 넣어줌과 동시에, apiSafeCall을 호출
*   2) apiSafeCall이 리턴한 걸 액티비티/프래그먼트로 전달
*   3) 액티비티에서 인자로 넣어준 요청 파라미터를 MultiPart form-data 형태로 변환
*
* 함수명 : 메서드 종류 + 도메인 종류 (+ 상세 설명)
*
* */

object ApiRepository {
    private val apiService: ApiService = ApiClient.instance.create(ApiService::class.java)

    suspend fun postAnalyze(imageFiles: List<File>): ApiResult<AnalyzeResponse> {
        val result =
            apiSafeCall { // result -> 서버 요청한 뒤의 결과를 저장
                // 서버로 보내줘야 하는 데이터 -> MultiPartBody로 변환
                val imageParts =
                    imageFiles.map { file ->
                        val reqFile = file.asRequestBody("image/*".toMediaType())
                        MultipartBody.Part.createFormData("images[]", file.name, reqFile)
                    }

                // ApiService 인터페이스에 선언된 함수 호출하여 POST 요청
                apiService.postAnalyzeRaw(
                    imageParts,
                )
            }
        return result
    }

    // 제품명 OCR 수행 요청
    suspend fun postName(imageFile: File): ApiResult<NameResponse> {
        val result =
            apiSafeCall { // result -> 서버 요청한 뒤의 결과를 저장
                // 서버로 보내줘야 하는 데이터 -> MultiPartBody로 변환
                val imageParts =
                    MultipartBody.Part.createFormData(
                        "image",
                        imageFile.name,
                        imageFile.asRequestBody("image/*".toMediaType()),
                    )

                // ApiService 인터페이스에 선언된 함수 호출하여 POST 요청
                apiService.postNameRaw(imageParts)
            }
        return result
    }
}
