package com.example.smartexpensemanager.data.local

import androidx.room.TypeConverter
import com.example.smartexpensemanager.data.local.entity.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
