package com.example.snapproject.api

import com.example.snapproject.model.AnalyzeResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/*
*
* 1) 이 파일을 따로 만든 이유 : 여기 작성해 둔 코드를 액티비티/프래그먼트에서 다 처리하게 하면 너무 복잡해져서 따로 분리했습니다.
*
* 2) 이 파일의 역할
*   2-1) 인터페이스에 선언된 메서드를 apiSafeCall의 인자로 넣어줌과 동시에, apiSafeCall을 호출
*   2-2) apiSafeCall이 리턴한 걸 액티비티/프래그먼트로 전달
*   2-3) 액티비티에서 인자로 넣어준 요청 파라미터를 MultiPart form-data 형태로 변환
*   2-4) Analyze 도메인으로 POST 요청할 때의 코루틴 스코프 관리
*   2-5) Analyze 도메인으로 POST 요청한 뒤, 응답 데이터 도착 시 -> MutableStateFlow 변수의 값을 업데이트하는 역할
*
* 3) 함수명 이름 기준 : 메서드 종류 + 도메인 종류 (+ 상세 설명)
*
* */

object ApiRepository {
    private val apiService: ApiService = ApiClient.instance.create(ApiService::class.java)

    // 1. "발표 영상 업로드 및 분석 수행" 요청 -> POST + analyze 도메인
    suspend fun postAnalyze(
        deviceUuid: String,
        imageFiles: List<File>,
    ): ApiResult<AnalyzeResponse> =
        apiSafeCall { // result -> 서버 요청한 뒤의 결과를 저장
            // MultiPart form-data의 Requestbody (디바이스 uuid, 스크립트 ID, 아이컨택 비율)
            val deviceUuidBody =
                deviceUuid.toRequestBody("text/plain".toMediaType()) // 디바이스 uuid

            // 서버로 보내줘야 하는 데이터들 -> MultiPartBody, RequestBody로 변환
            val imageParts =
                imageFiles.map { file ->
                    val reqFile = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("images[]", file.name, reqFile)
                }

            // ApiService 인터페이스에 선언된 함수 호출하여 POST 요청
            apiService.postAnalyzeRaw(
                deviceUuidBody,
                imageParts,
            )
        }
}
