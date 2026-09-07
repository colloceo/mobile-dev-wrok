package com.example.billreminder.util

import java.util.Locale

object CurrencyFormatter {
    fun withCode(amount: Double, currencyCode: String): String =
        String.format(Locale.US, "%s %,.2f", currencyCode, amount)
}
