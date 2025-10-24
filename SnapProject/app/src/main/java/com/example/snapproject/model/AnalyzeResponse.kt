package com.example.snapproject.model

import com.google.gson.annotations.SerializedName

data class AnalyzeResponse(
    val data: Data,
    val message: String,
    val status: Int,
    val success: Boolean,
) {
    data class Data(
        @SerializedName("expiration_date")
        val expirationDate: String,
        val ingredients: String,
        @SerializedName("product_name")
        val productName: String,
        val summary: List<String>,
    )
}
