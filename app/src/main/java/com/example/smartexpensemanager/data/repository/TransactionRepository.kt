package com.example.smartexpensemanager.data.repository

import com.example.smartexpensemanager.data.local.dao.TransactionDao
import com.example.smartexpensemanager.data.local.entity.TransactionEntity
import com.example.smartexpensemanager.data.local.entity.TransactionWithCategory
import com.example.smartexpensemanager.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BalanceSummary(val income: Double, val expense: Double) {
    val balance: Double get() = income - expense
}

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun observeTransactions(userId: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.observeForUser(userId)

    fun observeSummary(userId: Long): Flow<BalanceSummary> =
        transactionDao.observeTotalIncome(userId).combine(transactionDao.observeTotalExpense(userId)) { income, expense ->
            BalanceSummary(income, expense)
        }

    suspend fun getById(id: Long): TransactionEntity? = transactionDao.getById(id)

    suspend fun save(transaction: TransactionEntity): Result<Long> {
        return try {
            val id = if (transaction.id == 0L) {
                transactionDao.insert(transaction)
            } else {
                transactionDao.update(transaction)
                transaction.id
            }
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }

    suspend fun delete(transaction: TransactionEntity): Result<Unit> {
        return try {
            transactionDao.delete(transaction)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }
}
