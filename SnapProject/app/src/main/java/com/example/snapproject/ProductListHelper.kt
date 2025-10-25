package com.example.snapproject

import android.content.Context
import com.example.snapproject.model.db.Product
import com.example.snapproject.model.db.ProductDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ProductListHelper {

    // "오늘 날짜" -> yyyy.MM.dd 형태로 변환 후 리턴
    fun getTodayDateStr() : String {
        val today = Date() // 오늘 날짜
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) // YYYY.MM.DD 형태로 변환해주는 formatter
        return formatter.format(today)
    }

    // "오늘의 7일 후 날짜" 계산 -> yyyy.MM.dd 형태로 변환 후 리턴
    fun getSevenDaysLaterDateStr() : String {
        val calender = Calendar.getInstance()
        calender.time = Date()
        calender.add(Calendar.DAY_OF_YEAR, 7)
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) // YYYY.MM.DD 형태로 변환해주는 formatter
        return formatter.format(calender.time)
    }

    // db에서 날짜 지난 제품 목록 불러오기
    fun getListGone(context: Context, todayStr: String): List<Product> {
        val db = ProductDatabase.getInstance(context)
        return db?.productDao()?.getListGone(todayStr) ?: emptyList()
    }

    // db에서 날짜 임박 (7일 이하) 제품 목록 불러오기
    fun getListImminent(context: Context, todayStr: String, sevenDaysLaterStr: String): List<Product> {
        val db = ProductDatabase.getInstance(context)
        return db?.productDao()?.getListImminent(todayStr, sevenDaysLaterStr) ?: emptyList()
    }

    // db에서 날짜 여유 (7일 초과) 제품 목록 불러오기
    fun getListPlenty(context: Context, sevenDaysLaterStr: String): List<Product> {
        val db = ProductDatabase.getInstance(context)
        return db?.productDao()?.getListPlenty(sevenDaysLaterStr) ?: emptyList()
    }

    // 날짜 지난 제품 개수 계산
    fun getCountGone(context: Context, todayStr: String): Int {
        val db = ProductDatabase.getInstance(context)
        return db?.productDao()?.getCountGone(todayStr) ?: 0
    }

    // 날짜 임박 (7일 이하) 제품 개수 계산
    fun getCountImminent(context: Context, todayStr: String, sevenDaysLaterStr: String): Int {
        val db = ProductDatabase.getInstance(context)
        return db?.productDao()?.getCountImminent(todayStr, sevenDaysLaterStr) ?: 0
    }

    // 날짜 여유 (7일 초과) 제품 개수 계산
    fun getCountPlenty(context: Context, sevenDaysLaterStr: String): Int {
        val db = ProductDatabase.getInstance(context)
        return db?.productDao()?.getCountPlenty(sevenDaysLaterStr) ?: 0
    }
}
