package com.example.snapproject.model

data class ListItemData(
    val itemId: Int,
    val productName: String,
    val expirationDate: String,
    var isDeleteChecked: Boolean,
)
