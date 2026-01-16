package com.example.snapproject

import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

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
    navExtras: Navigator.Extras? = null,
) {
    val action = currentDestination?.getAction(resId) ?: graph.getAction(resId)
    // [현재 fragment의 id != 이동할 fragment의 id]일 때만, 화면 이동
    if (action != null && currentDestination?.id != action.destinationId) {
        navigate(resId, args, navOptions, navExtras)
    }
}
