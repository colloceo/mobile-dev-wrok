package com.example.billreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.billreminder.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?
}
