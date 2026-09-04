package com.example.smartexpensemanager.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.smartexpensemanager.data.local.AppDatabase
import com.example.smartexpensemanager.data.local.dao.CategoryDao
import com.example.smartexpensemanager.data.local.dao.TransactionDao
import com.example.smartexpensemanager.data.local.dao.UserDao
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.data.local.entity.TransactionEntity
import com.example.smartexpensemanager.data.local.entity.TransactionType
import com.example.smartexpensemanager.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        categoryDao = db.categoryDao()
        transactionDao = db.transactionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertUpdateDelete_roundTrips() = runBlocking {
        val userId = userDao.insert(UserEntity(username = "alice", passwordHash = "h", salt = "s"))
        val categoryId = categoryDao.insert(CategoryEntity(name = "Food", type = TransactionType.EXPENSE, colorHex = "#EF5350"))

        val id = transactionDao.insert(
            TransactionEntity(
                userId = userId,
                categoryId = categoryId,
                type = TransactionType.EXPENSE,
                amount = 25.0,
                note = "Lunch",
                dateMillis = 1000L
            )
        )

        val inserted = transactionDao.getById(id)
        assertEquals(25.0, inserted?.amount)

        transactionDao.update(inserted!!.copy(amount = 40.0))
        assertEquals(40.0, transactionDao.getById(id)?.amount)

        val withCategory = transactionDao.observeForUser(userId).first()
        assertEquals(1, withCategory.size)
        assertEquals("Food", withCategory.first().category.name)

        transactionDao.delete(transactionDao.getById(id)!!)
        assertNull(transactionDao.getById(id))
    }
}
