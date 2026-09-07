package com.example.billreminder.data.local

import androidx.room.TypeConverter
import com.example.billreminder.data.local.entity.BillCategory
import com.example.billreminder.data.local.entity.Recurrence

class Converters {
    @TypeConverter
    fun fromBillCategory(value: BillCategory): String = value.name

    @TypeConverter
    fun toBillCategory(value: String): BillCategory = BillCategory.valueOf(value)

    @TypeConverter
    fun fromRecurrence(value: Recurrence): String = value.name

    @TypeConverter
    fun toRecurrence(value: String): Recurrence = Recurrence.valueOf(value)
}
