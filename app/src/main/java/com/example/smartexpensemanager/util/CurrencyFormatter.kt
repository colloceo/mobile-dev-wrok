package com.example.smartexpensemanager.util

import java.util.Locale

/**
 * Formats amounts as Kenyan Shillings with a fixed "KSh" prefix and grouped
 * digits, independent of the device's locale (relying on the default
 * currency locale is unreliable across OEMs/API levels for KES).
 */
object CurrencyFormatter {
    fun kes(amount: Double): String = String.format(Locale.US, "KSh %,.2f", amount)

    fun withCode(amount: Double, currencyCode: String): String =
        String.format(Locale.US, "%s %,.2f", currencyCode, amount)
}
