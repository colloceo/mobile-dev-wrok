package com.example.billreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.billreminder.data.local.entity.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Insert
    suspend fun insert(bill: BillEntity): Long

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BillEntity?

    @Query("SELECT * FROM bills WHERE userId = :userId AND isActive = 1 ORDER BY nextDueDateMillis ASC")
    fun observeActiveForUser(userId: Long): Flow<List<BillEntity>>

    @Query("SELECT COUNT(*) FROM payments WHERE billId = :billId")
    suspend fun paymentCountForBill(billId: Long): Int
}
