package com.example.billreminder.data.repository

import com.example.billreminder.data.local.dao.UserDao
import com.example.billreminder.data.local.entity.UserEntity
import com.example.billreminder.util.PasswordHasher
import com.example.billreminder.util.Result

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(username: String, password: String): Result<UserEntity> {
        return try {
            if (userDao.findByUsername(username) != null) {
                return Result.Error("username_taken")
            }
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(password, salt)
            val user = UserEntity(username = username, passwordHash = hash, salt = salt)
            val id = userDao.insert(user)
            Result.Success(user.copy(id = id))
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }

    suspend fun login(username: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.findByUsername(username)
                ?: return Result.Error("invalid_login")
            if (!PasswordHasher.matches(password, user.salt, user.passwordHash)) {
                return Result.Error("invalid_login")
            }
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e.message ?: "database_error")
        }
    }
}
