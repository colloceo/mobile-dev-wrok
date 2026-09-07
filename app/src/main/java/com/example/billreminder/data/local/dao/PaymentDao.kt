package com.example.billreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.billreminder.data.local.entity.PaymentWithBill
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: com.example.billreminder.data.local.entity.PaymentEntity): Long

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query(
        "SELECT * FROM payments WHERE billId IN (SELECT id FROM bills WHERE userId = :userId) " +
            "ORDER BY paidDateMillis DESC"
    )
    fun observeForUser(userId: Long): Flow<List<PaymentWithBill>>
}
