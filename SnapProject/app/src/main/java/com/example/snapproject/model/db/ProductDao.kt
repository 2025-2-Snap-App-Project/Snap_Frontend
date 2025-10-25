package com.example.snapproject.model.db

import androidx.room.Dao
import androidx.room.Insert

// Data Access Object 인터페이스
@Dao
interface ProductDao {
    // 레코드 삽입 메서드
    @Insert
    fun insert(product: Product)
}
