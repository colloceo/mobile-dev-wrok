package com.example.billreminder.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.billreminder.data.local.AppDatabase
import com.example.billreminder.data.local.dao.BillDao
import com.example.billreminder.data.local.dao.PaymentDao
import com.example.billreminder.data.local.dao.UserDao
import com.example.billreminder.data.local.entity.BillCategory
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.data.local.entity.PaymentEntity
import com.example.billreminder.data.local.entity.Recurrence
import com.example.billreminder.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BillDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var billDao: BillDao
    private lateinit var paymentDao: PaymentDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
        billDao = db.billDao()
        paymentDao = db.paymentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertUpdateDelete_roundTrips() = runBlocking {
        val userId = userDao.insert(UserEntity(username = "alice", passwordHash = "h", salt = "s"))

        val billId = billDao.insert(
            BillEntity(
                userId = userId,
                name = "Netflix",
                amount = 1100.0,
                currencyCode = "KES",
                category = BillCategory.SUBSCRIPTION,
                recurrence = Recurrence.MONTHLY,
                nextDueDateMillis = 1000L
            )
        )

        val inserted = billDao.getById(billId)
        assertEquals(1100.0, inserted?.amount)

        billDao.update(inserted!!.copy(amount = 1200.0))
        assertEquals(1200.0, billDao.getById(billId)?.amount)

        val active = billDao.observeActiveForUser(userId).first()
        assertEquals(1, active.size)
        assertEquals("Netflix", active.first().name)

        billDao.delete(billDao.getById(billId)!!)
        assertNull(billDao.getById(billId))
    }

    @Test
    fun markPaid_rollsForwardAndRecordsPayment() = runBlocking {
        val userId = userDao.insert(UserEntity(username = "bob", passwordHash = "h", salt = "s"))
        val billId = billDao.insert(
            BillEntity(
                userId = userId,
                name = "KPLC",
                amount = 800.0,
                currencyCode = "KES",
                category = BillCategory.UTILITY,
                recurrence = Recurrence.MONTHLY,
                nextDueDateMillis = 5000L
            )
        )

        paymentDao.insert(PaymentEntity(billId = billId, amountPaid = 800.0, currencyCode = "KES", paidDateMillis = 6000L))

        val payments = paymentDao.observeForUser(userId).first()
        assertEquals(1, payments.size)
        assertEquals("KPLC", payments.first().bill.name)
    }
}
