package com.example.smartexpensemanager.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class PreferencesManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        set(value) {
            prefs.edit { putString(KEY_THEME_MODE, value.name) }
            applyThemeMode(value)
        }

    var defaultCurrency: String
        get() = prefs.getString(KEY_DEFAULT_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
        set(value) = prefs.edit { putString(KEY_DEFAULT_CURRENCY, value) }

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_DONE, value) }

    fun applyStoredThemeMode() {
        applyThemeMode(themeMode)
    }

    private fun applyThemeMode(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DEFAULT_CURRENCY = "default_currency"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        const val DEFAULT_CURRENCY = "USD"
    }
}
