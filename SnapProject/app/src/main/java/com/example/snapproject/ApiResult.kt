package com.example.snapproject

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/*
*
* 서버에서 응답 왔을 때, 에러 핸들링을 좀 더 수월하게 하기 위해 이 파일을 따로 추가했습니다.
*
* 1) ApiService 인터페이스에 선언된 메서드를 apiSafeCall의 인자로 넘겨줌.
* 2) ApiRepository의 메서드가 apiSafeCall 함수를 호출
* 3) apiSafeCall은 "서버 통신 성공 / 실패" 여부에 따라 리턴하는 게 달라짐
*   3-1) 서버 통신 성공 시 : ApiResult<응답 데이터 클래스>.Success(응답 Response) -> 얘를 반환함.
*   3-2) 서버 통신 실패 시 : ApiResult<Nothing>.Error(상태 코드, 에러 메시지) -> 얘를 반환함.
*
* 4) ApiRepository의 메서드는 apiSafeCall이 리턴한 것을 액티비티/프래그먼트로 전달을 해주는 역할
*
* */

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>() // 서버 통신 성공 시 반환 타입

    data class Error(val code: Int?, val message: String) : ApiResult<Nothing>() // 서버 통신 실패 시 반환 타입
}

// 반환 타입 : ApiResult<응답 data class>.Success(응답 body) or ApiResult<Nothing>.Error(상태 코드, 에러 메시지)
// 여기 선언된 apiSafeCall을 ApiRespository에서 선언된 메서드들이 각각 호출하는 방식

suspend fun <T : Any> apiSafeCall(call: suspend () -> Response<T>): ApiResult<T> {
    val nullErrMsg = "알 수 없는 에러" // 에러 메시지가 없는 경우 반환할 message
    val nullBodyMsg = "응답 Body가 없습니다." // 응답 body가 null일 때 반환할 message

    return try {
        val myRes = call.invoke() // Response
        val resBody = myRes.body() // Response body

        val resCode = myRes.code() // Response 상태 코드
        val resErrBody = myRes.errorBody()?.string() // Response 에러 Body
        val resMsg = myRes.message() // Response 메시지

        when {
            myRes.isSuccessful -> { // 응답 성공 -> 응답 Body 반환
                // 만약에 응답 body가 null이면 -> 상태 코드 + 에러 메시지(응답 바디 X) 반환
                resBody?.let { ApiResult.Success(it) } ?: ApiResult.Error(resCode, nullBodyMsg)
            }

            else -> { // 응답 실패 -> "상태 코드 + 응답 에러 body" 반환
                // 응답에 에러 body 없으면 -> 에러 메시지 / 에러 메시지도 없으면 -> "알 수 없는 에러"
                ApiResult.Error(resCode, resErrBody ?: resMsg ?: nullErrMsg)
            }
        }
    } catch (e: IOException) {
        // 네트워크 에러
        ApiResult.Error(null, "네트워크 에러. ${e.message ?: nullErrMsg}") // 에러 메시지
    } catch (e: HttpException) {
        // HttpException 오류 (4XX, 5XX 등)
        ApiResult.Error(e.code(), e.message ?: nullErrMsg) // 상태 코드 + 에러 메시지
    } catch (e: CancellationException) {
        // 코루틴 취소 시 발생
        throw e
    } catch (e: Exception) {
        // 그 외의 에러 처리
        ApiResult.Error(null, e.message ?: nullErrMsg) // 에러 메시지
    }
}
