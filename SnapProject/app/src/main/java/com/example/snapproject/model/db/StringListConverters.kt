package com.example.snapproject.model.db

import androidx.room.TypeConverter
import com.google.gson.Gson

// List<String> Converter 클래스
class StringListConverters {
    @TypeConverter
    fun listToJson(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun jsonToList(value: String): List<String>? {
        return Gson().fromJson(value,Array<String>::class.java)?.toList()
    }
}
