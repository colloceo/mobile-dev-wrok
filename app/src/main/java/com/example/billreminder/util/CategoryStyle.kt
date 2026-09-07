package com.example.billreminder.util

import com.example.billreminder.R
import com.example.billreminder.data.local.entity.BillCategory

/**
 * Categories are distinguished by icon glyph only — deliberately no per-category
 * color coding, to keep the palette to one accent plus neutral grays.
 */
object CategoryStyle {

    fun iconFor(category: BillCategory): Int = when (category) {
        BillCategory.UTILITY -> R.drawable.ic_bolt
        BillCategory.SUBSCRIPTION -> R.drawable.ic_repeat
        BillCategory.RENT -> R.drawable.ic_building
        BillCategory.LOAN -> R.drawable.ic_bank
        BillCategory.OTHER -> R.drawable.ic_receipt
    }

    fun labelRes(category: BillCategory): Int = when (category) {
        BillCategory.UTILITY -> R.string.category_utility
        BillCategory.SUBSCRIPTION -> R.string.category_subscription
        BillCategory.RENT -> R.string.category_rent
        BillCategory.LOAN -> R.string.category_loan
        BillCategory.OTHER -> R.string.category_other
    }
}
