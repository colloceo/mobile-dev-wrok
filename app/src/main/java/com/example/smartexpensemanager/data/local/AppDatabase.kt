package com.example.smartexpensemanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartexpensemanager.data.local.dao.CategoryDao
import com.example.smartexpensemanager.data.local.dao.TransactionDao
import com.example.smartexpensemanager.data.local.dao.UserDao
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.data.local.entity.TransactionEntity
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val defaultCategories = listOf(
            CategoryEntity(name = "Salary", type = TransactionType.INCOME, colorHex = "#66BB6A"),
            CategoryEntity(name = "Other Income", type = TransactionType.INCOME, colorHex = "#26A69A"),
            CategoryEntity(name = "Food", type = TransactionType.EXPENSE, colorHex = "#EF5350"),
            CategoryEntity(name = "Transport", type = TransactionType.EXPENSE, colorHex = "#42A5F5"),
            CategoryEntity(name = "Bills", type = TransactionType.EXPENSE, colorHex = "#FFA726"),
            CategoryEntity(name = "Shopping", type = TransactionType.EXPENSE, colorHex = "#AB47BC"),
            CategoryEntity(name = "Other Expense", type = TransactionType.EXPENSE, colorHex = "#8D6E63")
        )

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_expense_manager.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = INSTANCE?.categoryDao() ?: return@launch
                            if (dao.count() == 0) {
                                defaultCategories.forEach { dao.insert(it) }
                            }
                        }
                    }
                }).build().also { INSTANCE = it }
            }
        }
    }
}
