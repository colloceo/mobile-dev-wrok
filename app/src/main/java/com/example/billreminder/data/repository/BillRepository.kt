package com.example.billreminder.data.repository

import com.example.billreminder.data.local.dao.BillDao
import com.example.billreminder.data.local.dao.PaymentDao
import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.data.local.entity.PaymentEntity
import com.example.billreminder.data.local.entity.Recurrence
import com.example.billreminder.util.Result
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class MarkPaidResult(val paymentId: Long, val previousBill: BillEntity)

class BillRepository(
    private val billDao: BillDao,
    private val paymentDao: PaymentDao
) {

    fun observeActiveBills(userId: Long): Flow<List<BillEntity>> = billDao.observeActiveForUser(userId)

    suspend fun getById(id: Long): BillEntity? = billDao.getById(id)

    suspend fun save(bill: BillEntity): Result<Long> {
        return try {
            val id = if (bill.id == 0L) {
                billDao.insert(bill)
            } else {
                billDao.update(bill)
                bill.id
            }
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }

    suspend fun delete(bill: BillEntity): Result<Unit> {
        return try {
            billDao.delete(bill)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }

    suspend fun markPaid(bill: BillEntity): Result<MarkPaidResult> {
        return try {
            val paymentId = paymentDao.insert(
                PaymentEntity(
                    billId = bill.id,
                    amountPaid = bill.amount,
                    currencyCode = bill.currencyCode,
                    paidDateMillis = System.currentTimeMillis()
                )
            )
            val updated = when (bill.recurrence) {
                Recurrence.ONE_TIME -> bill.copy(isActive = false)
                else -> bill.copy(nextDueDateMillis = rollForward(bill.nextDueDateMillis, bill.recurrence))
            }
            billDao.update(updated)
            Result.Success(MarkPaidResult(paymentId, bill))
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }

    suspend fun undoMarkPaid(result: MarkPaidResult): Result<Unit> {
        return try {
            paymentDao.deleteById(result.paymentId)
            billDao.update(result.previousBill)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }

    private fun rollForward(fromMillis: Long, recurrence: Recurrence): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = fromMillis }
        when (recurrence) {
            Recurrence.WEEKLY -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            Recurrence.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            Recurrence.YEARLY -> calendar.add(Calendar.YEAR, 1)
            Recurrence.ONE_TIME -> Unit
        }
        return calendar.timeInMillis
    }
}
