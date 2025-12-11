package com.example.snapproject.model

import com.google.gson.annotations.SerializedName

data class NameResponse(
    val message: String,
    @SerializedName("product_name")
    val productName: String,
    val status: Int,
    val success: Boolean,
)
