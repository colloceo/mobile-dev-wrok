package com.example.billreminder.util

import android.app.Activity
import android.content.Intent
import com.example.billreminder.R
import com.example.billreminder.ui.currency.CurrencyActivity
import com.example.billreminder.ui.dashboard.DashboardActivity
import com.example.billreminder.ui.history.PaymentHistoryActivity
import com.example.billreminder.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

enum class TopLevelDestination { HOME, HISTORY, CURRENCY, SETTINGS }

object BottomNavHelper {

    fun setup(activity: Activity, bottomNav: BottomNavigationView, current: TopLevelDestination) {
        bottomNav.selectedItemId = idFor(current)
        bottomNav.setOnItemSelectedListener { item ->
            val destination = destinationFor(item.itemId) ?: return@setOnItemSelectedListener false
            if (destination != current) {
                navigateTo(activity, destination)
            }
            true
        }
    }

    private fun idFor(destination: TopLevelDestination) = when (destination) {
        TopLevelDestination.HOME -> R.id.nav_home
        TopLevelDestination.HISTORY -> R.id.nav_history
        TopLevelDestination.CURRENCY -> R.id.nav_currency
        TopLevelDestination.SETTINGS -> R.id.nav_settings
    }

    private fun destinationFor(itemId: Int) = when (itemId) {
        R.id.nav_home -> TopLevelDestination.HOME
        R.id.nav_history -> TopLevelDestination.HISTORY
        R.id.nav_currency -> TopLevelDestination.CURRENCY
        R.id.nav_settings -> TopLevelDestination.SETTINGS
        else -> null
    }

    private fun navigateTo(activity: Activity, destination: TopLevelDestination) {
        val targetClass = when (destination) {
            TopLevelDestination.HOME -> DashboardActivity::class.java
            TopLevelDestination.HISTORY -> PaymentHistoryActivity::class.java
            TopLevelDestination.CURRENCY -> CurrencyActivity::class.java
            TopLevelDestination.SETTINGS -> SettingsActivity::class.java
        }
        val intent = Intent(activity, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(intent)
    }
}
