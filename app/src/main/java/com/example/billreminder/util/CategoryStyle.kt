package com.example.billreminder.util

import com.example.billreminder.R
import com.example.billreminder.data.local.entity.BillCategory

object CategoryStyle {

    fun iconFor(category: BillCategory): Int = when (category) {
        BillCategory.UTILITY -> R.drawable.ic_bolt
        BillCategory.SUBSCRIPTION -> R.drawable.ic_repeat
        BillCategory.RENT -> R.drawable.ic_building
        BillCategory.LOAN -> R.drawable.ic_bank
        BillCategory.OTHER -> R.drawable.ic_receipt
    }

    fun colorFor(category: BillCategory): Int = when (category) {
        BillCategory.UTILITY -> R.color.cat_utility
        BillCategory.SUBSCRIPTION -> R.color.cat_subscription
        BillCategory.RENT -> R.color.cat_rent
        BillCategory.LOAN -> R.color.cat_loan
        BillCategory.OTHER -> R.color.cat_other
    }

    fun softColorFor(category: BillCategory): Int = when (category) {
        BillCategory.UTILITY -> R.color.cat_utility_soft
        BillCategory.SUBSCRIPTION -> R.color.cat_subscription_soft
        BillCategory.RENT -> R.color.cat_rent_soft
        BillCategory.LOAN -> R.color.cat_loan_soft
        BillCategory.OTHER -> R.color.cat_other_soft
    }

    fun labelRes(category: BillCategory): Int = when (category) {
        BillCategory.UTILITY -> R.string.category_utility
        BillCategory.SUBSCRIPTION -> R.string.category_subscription
        BillCategory.RENT -> R.string.category_rent
        BillCategory.LOAN -> R.string.category_loan
        BillCategory.OTHER -> R.string.category_other
    }
}
