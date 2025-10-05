package com.example.snapproject.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "" // 실제 서버 주소

    // HttpLoggingInterceptor 인스턴스 생성 & 레벨 설정
    // "okhttp.OkHttpClient"로 로그 검색
    private val logging =
        HttpLoggingInterceptor(PrettyJsonLogger()).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    // OkHttp Client
    private val okHttp =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS) // 서버 연결 최대 10초 수행
            .writeTimeout(120, TimeUnit.SECONDS) // 서버 요청 최대 2분 수행 -> ex) 오디오 파일 업로드
            .readTimeout(600, TimeUnit.SECONDS) // 서버 응답 최대 10분 수행 -> ex) AI 발표 분석(Whisper 등) 후 응답
            .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttp)
            .build()
    }
}
