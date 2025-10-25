package com.example.snapproject.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// 데이터베이스 관리 객체
@Database(entities = [Product::class], version = 1)
@TypeConverters(StringListConverters::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        private var instance: ProductDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ProductDatabase? {
            if (instance == null) {
                synchronized(ProductDatabase::class) {
                    instance =
                        Room.databaseBuilder(
                            context.applicationContext,
                            ProductDatabase::class.java,
                            "storage-database",
                        ).allowMainThreadQueries().build()
                }
            }
            return instance
        }
    }
}
