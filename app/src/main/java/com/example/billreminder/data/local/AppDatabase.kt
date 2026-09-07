package com.example.billreminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.billreminder.data.local.dao.BillDao
import com.example.billreminder.data.local.dao.PaymentDao
import com.example.billreminder.data.local.dao.UserDao
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.data.local.entity.PaymentEntity
import com.example.billreminder.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, BillEntity::class, PaymentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun billDao(): BillDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bill_reminder.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
