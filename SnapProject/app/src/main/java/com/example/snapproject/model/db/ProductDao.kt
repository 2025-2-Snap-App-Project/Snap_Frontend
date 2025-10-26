package com.example.snapproject.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// Data Access Object 인터페이스
@Dao
interface ProductDao {
    // 레코드 삽입 메서드
    @Insert
    fun insert(product: Product)

    // "날짜 지남" 레코드 조회 메서드
    @Query("SELECT * FROM ProductTable WHERE expirationDate < :today")
    fun getListGone(today: String): List<Product>

    // "날짜 임박"(7일 이하) 레코드 조회 메서드
    @Query("SELECT * FROM ProductTable WHERE expirationDate > :today AND expirationDate <= :sevenDaysLater")
    fun getListImminent(
        today: String,
        sevenDaysLater: String,
    ): List<Product>

    // "날짜 여유"(7일 초과) 레코드 조회 메서드
    @Query("SELECT * FROM ProductTable WHERE expirationDate > :sevenDaysLater")
    fun getListPlenty(sevenDaysLater: String): List<Product>

    // "날짜 지남" 레코드 개수 리턴
    @Query("SELECT COUNT(*) FROM ProductTable WHERE expirationDate < :today")
    fun getCountGone(today: String): Int

    // "날짜 임박"(7일 이하) 레코드 개수 리턴
    @Query("SELECT COUNT(*) FROM ProductTable WHERE expirationDate > :today AND expirationDate <= :sevenDaysLater")
    fun getCountImminent(
        today: String,
        sevenDaysLater: String,
    ): Int

    // "날짜 여유"(7일 초과) 레코드 개수 리턴
    @Query("SELECT COUNT(*) FROM ProductTable WHERE expirationDate > :sevenDaysLater")
    fun getCountPlenty(sevenDaysLater: String): Int

    // 선택한 제품 ID (1개 이상)에 해당하는 레코드 일괄 삭제
    @Query("DELETE FROM ProductTable WHERE productId IN (:itemIds)")
    fun deleteList(itemIds: List<Int>)

    // 특정 제품의 상세 정보 불러오기
    @Query("SELECT * FROM ProductTable WHERE productId = :itemId")
    fun getDetail(itemId: Int): Product

    // 특정 제품의 보관 장소 업데이트
    @Query("UPDATE ProductTable SET storageLocation = :storageLocation WHERE productId = :itemId")
    fun updateStorage(
        storageLocation: String,
        itemId: Int,
    )
}
