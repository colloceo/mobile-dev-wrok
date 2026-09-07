package com.example.billreminder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("billId")]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val billId: Long,
    val amountPaid: Double,
    val currencyCode: String,
    val paidDateMillis: Long
)
