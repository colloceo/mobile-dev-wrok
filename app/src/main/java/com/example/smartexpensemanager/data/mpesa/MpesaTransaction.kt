package com.example.smartexpensemanager.data.mpesa

import com.example.smartexpensemanager.data.local.entity.TransactionType

data class MpesaTransaction(
    val code: String,
    val type: TransactionType,
    val amount: Double,
    val counterparty: String,
    val dateMillis: Long
)
