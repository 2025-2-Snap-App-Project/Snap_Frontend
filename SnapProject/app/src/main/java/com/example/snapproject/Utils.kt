package com.example.snapproject

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

// TextView의 ** 사이에 텍스트 스타일(컬러, 폰트)를 적용하는 함수
fun applyStyleBetweenAsterisks(
    tv: TextView,
    context: Context,
) {
    val tvData: String = tv.text.toString()
    val tvBuilder = SpannableStringBuilder(tvData)

    var start = tvData.indexOf("**")
    var end = tvData.indexOf("**", start + 2)

    val textFont = Typeface.create(ResourcesCompat.getFont(context, R.font.pretendard_bold), Typeface.NORMAL)
    val textColor = "#2276FF".toColorInt()

    while (start != -1 && end != -1) {
        tvBuilder.setSpan(CustomTypefaceSpan(textFont), start + 2, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvBuilder.setSpan(ForegroundColorSpan(textColor), start + 2, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvBuilder.replace(end, end + 2, "")
        tvBuilder.replace(start, start + 2, "")

        start = tvBuilder.indexOf("**")
        end = tvBuilder.indexOf("**", start + 2)
    }
    tv.text = tvBuilder
}

// LinearGradient를 적용하는 별도의 확장 메소드 정의
fun TextView.setTextColorAsLinearGradient(colors: IntArray) {
    if (colors.isEmpty()) {
        return
    }

    setTextColor(colors[0])
    this.paint.shader =
        LinearGradient(
            0f,
            0f,
            paint.measureText(this.text.toString()),
            -this.textSize,
            colors,
            null,
            Shader.TileMode.CLAMP,
        )
}

// navigate() 대신 사용
fun NavController.navigateSafe(
    @IdRes resId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
    navExtras: Navigator.Extras? = null
) {
    val action = currentDestination?.getAction(resId) ?: graph.getAction(resId)
    // [현재 fragment의 id != 이동할 fragment의 id]일 때만, 화면 이동
    if (action != null && currentDestination?.id != action.destinationId) {
        navigate(resId, args, navOptions, navExtras)
    }
}
