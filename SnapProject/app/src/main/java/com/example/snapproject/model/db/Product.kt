package com.example.snapproject.model.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// 제품 Entity (DB의 테이블)
@Entity(tableName = "ProductTable")
data class Product(
    var storageLocation: String,
    var productName: String,
    var expirationDate: String,
    var summary: List<String>,
    var ingredients: String,
) {
    @PrimaryKey(autoGenerate = true)
    var productId: Int = 0
}
