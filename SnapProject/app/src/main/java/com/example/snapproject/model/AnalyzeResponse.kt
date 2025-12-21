package com.example.snapproject.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AnalyzeResponse(
    val data: Data,
    val message: String,
    val status: Int,
    val success: Boolean,
) : Serializable {
    data class Data(
        val ingredients: String,
        @SerializedName("product_name")
        val productName: String,
        val summary: List<String>,
    ) : Serializable
}
