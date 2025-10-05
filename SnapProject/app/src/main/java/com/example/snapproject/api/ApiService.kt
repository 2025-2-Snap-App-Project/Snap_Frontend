package com.example.snapproject.api

import com.example.snapproject.model.AnalyzeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    // ----------------------------------------------------

    // 함수명 뒤에 Raw를 붙인 이유 - ApiRepository.kt에서 다시 가공할 거라서
    // 함수명 : "메서드 종류 + 도메인 종류 (+상세 설명) + Raw" 로 작성했음.

    @Multipart
    @POST("analyze")
    suspend fun postAnalyzeRaw(
        @Part("device_id") deviceId: RequestBody,
        @Part images: List<MultipartBody.Part>,
    ): Response<AnalyzeResponse>
}
