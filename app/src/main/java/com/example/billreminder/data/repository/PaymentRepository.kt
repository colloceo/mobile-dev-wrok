package com.example.billreminder.data.repository

import com.example.billreminder.data.local.dao.PaymentDao
import com.example.billreminder.data.local.entity.PaymentWithBill
import com.example.billreminder.util.Result
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {

    fun observeForUser(userId: Long): Flow<List<PaymentWithBill>> = paymentDao.observeForUser(userId)

    suspend fun delete(paymentId: Long): Result<Unit> {
        return try {
            paymentDao.deleteById(paymentId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }
}
