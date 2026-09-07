package com.example.billreminder.ui.dashboard

import com.example.billreminder.data.local.entity.BillEntity
import com.example.billreminder.util.Urgency

sealed class BillListItem {
    data class SectionHeader(val label: String) : BillListItem()
    data class Row(val bill: BillEntity, val urgency: Urgency) : BillListItem()
}
