package com.example.billreminder.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PaymentWithBill(
    @Embedded val payment: PaymentEntity,
    @Relation(parentColumn = "billId", entityColumn = "id")
    val bill: BillEntity
)
