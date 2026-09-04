package com.example.smartexpensemanager.util

import android.app.Activity
import android.content.Intent
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.ui.category.CategoryActivity
import com.example.smartexpensemanager.ui.dashboard.DashboardActivity
import com.example.smartexpensemanager.ui.rates.ExchangeRateActivity
import com.example.smartexpensemanager.ui.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

enum class TopLevelDestination { HOME, CATEGORIES, RATES, SETTINGS }

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
        TopLevelDestination.CATEGORIES -> R.id.nav_categories
        TopLevelDestination.RATES -> R.id.nav_rates
        TopLevelDestination.SETTINGS -> R.id.nav_settings
    }

    private fun destinationFor(itemId: Int) = when (itemId) {
        R.id.nav_home -> TopLevelDestination.HOME
        R.id.nav_categories -> TopLevelDestination.CATEGORIES
        R.id.nav_rates -> TopLevelDestination.RATES
        R.id.nav_settings -> TopLevelDestination.SETTINGS
        else -> null
    }

    private fun navigateTo(activity: Activity, destination: TopLevelDestination) {
        val targetClass = when (destination) {
            TopLevelDestination.HOME -> DashboardActivity::class.java
            TopLevelDestination.CATEGORIES -> CategoryActivity::class.java
            TopLevelDestination.RATES -> ExchangeRateActivity::class.java
            TopLevelDestination.SETTINGS -> SettingsActivity::class.java
        }
        val intent = Intent(activity, targetClass).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        activity.startActivity(intent)
    }
}
