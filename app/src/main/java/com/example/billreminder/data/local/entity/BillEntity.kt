package com.example.billreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BillCategory { UTILITY, SUBSCRIPTION, RENT, LOAN, OTHER }

enum class Recurrence { ONE_TIME, WEEKLY, MONTHLY, YEARLY }

@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val amount: Double,
    val currencyCode: String,
    val category: BillCategory,
    val recurrence: Recurrence,
    val nextDueDateMillis: Long,
    val isActive: Boolean = true,
    val billerPhone: String? = null,
    val billerEmail: String? = null,
    val notes: String = ""
)
