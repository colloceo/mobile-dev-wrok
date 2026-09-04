package com.example.smartexpensemanager.data.repository

import com.example.smartexpensemanager.data.local.dao.CategoryDao
import com.example.smartexpensemanager.data.local.entity.CategoryEntity
import com.example.smartexpensemanager.util.Result
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun getAll(): List<CategoryEntity> = categoryDao.getAll()

    suspend fun save(category: CategoryEntity): Result<Long> {
        return try {
            val id = if (category.id == 0L) {
                categoryDao.insert(category)
            } else {
                categoryDao.update(category)
                category.id
            }
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }

    suspend fun delete(category: CategoryEntity): Result<Unit> {
        return try {
            if (categoryDao.transactionCountForCategory(category.id) > 0) {
                return Result.Error("category_in_use")
            }
            categoryDao.delete(category)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }
}
