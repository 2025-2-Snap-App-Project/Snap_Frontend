package com.example.snapproject

import android.graphics.LinearGradient
import android.graphics.Shader
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt

class LinearGradientSpan(
    private val containingText: String,
    private val textToStyle: String,
    @ColorInt private val startColor: Int,
    @ColorInt private val centerColor: Int,
    @ColorInt private val endColor: Int,
) : CharacterStyle(), UpdateAppearance {
    override fun updateDrawState(tp: TextPaint?) {
        tp ?: return

        var leadingWidth = 0f
        val indexOfTextToStyle = containingText.indexOf(textToStyle)
        if (!containingText.startsWith(textToStyle) && containingText != textToStyle) {
            leadingWidth = tp.measureText(containingText, 0, indexOfTextToStyle)
        }
        val gradientWidth = tp.measureText(containingText, indexOfTextToStyle, indexOfTextToStyle + textToStyle.length)
        val colorArray = IntArray(3) { 0 }
        colorArray[0] = startColor
        colorArray[1] = centerColor
        colorArray[2] = endColor

        tp.shader = LinearGradient(
            leadingWidth,
            0f,
            leadingWidth + gradientWidth,
            0f,
            colorArray, null,
            Shader.TileMode.REPEAT
        )
    }
}
