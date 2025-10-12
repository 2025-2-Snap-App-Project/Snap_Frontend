package com.example.snapproject

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    // SharedPreference에 기록된 Boolean 값 가져오기
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    // SharedPreference에 Boolean 값 기록하기
    fun setBoolean(key: String, boolean: Boolean) {
        prefs.edit() { putBoolean(key, boolean) }
    }
}
