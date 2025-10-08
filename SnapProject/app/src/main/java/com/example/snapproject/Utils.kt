package com.example.snapproject

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt

// TextView의 ** 사이에 텍스트 스타일(컬러, 폰트)를 적용하는 함수
fun applyStyleBetweenAsterisks(tv: TextView, context: Context) {
    val tvData: String = tv.text.toString()
    val tvBuilder = SpannableStringBuilder(tvData)

    var start = tvData.indexOf("**")
    var end = tvData.indexOf("**", start + 2)

    val textFont = Typeface.create(ResourcesCompat.getFont(context, R.font.pretendard_bold), Typeface.NORMAL)
    val textColor = "#2276FF".toColorInt()

    while (start != -1 && end != -1) {
        tvBuilder.setSpan(CustomTypefaceSpan(textFont), start+2, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvBuilder.setSpan(ForegroundColorSpan(textColor), start+2, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvBuilder.replace(end, end + 2, "")
        tvBuilder.replace(start, start + 2, "")

        start = tvBuilder.indexOf("**")
        end = tvBuilder.indexOf("**", start + 2)
    }
    tv.text = tvBuilder
}
