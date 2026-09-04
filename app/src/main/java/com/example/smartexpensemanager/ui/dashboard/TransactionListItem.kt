package com.example.smartexpensemanager.ui.dashboard

import com.example.smartexpensemanager.data.local.entity.TransactionWithCategory

sealed class TransactionListItem {
    data class DateHeader(val label: String) : TransactionListItem()
    data class Row(val item: TransactionWithCategory) : TransactionListItem()
}
