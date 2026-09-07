package com.example.billreminder.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.billreminder.BuildConfig
import com.example.billreminder.R
import com.example.billreminder.databinding.ActivitySettingsBinding
import com.example.billreminder.ui.auth.LoginActivity
import com.example.billreminder.util.BillCurrencies
import com.example.billreminder.util.BottomNavHelper
import com.example.billreminder.util.ThemeMode
import com.example.billreminder.util.TopLevelDestination
import com.example.billreminder.util.app

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        BottomNavHelper.setup(this, binding.bottomNav, TopLevelDestination.SETTINGS)

        binding.textUsername.text = app.sessionManager.loggedInUsername.orEmpty()
        binding.textVersion.text = getString(R.string.settings_about_version) + " " + BuildConfig.VERSION_NAME

        refreshThemeLabel()
        refreshCurrencyLabel()
        refreshWarningDaysLabel()

        binding.rowTheme.setOnClickListener { showThemeDialog() }
        binding.rowCurrency.setOnClickListener { showCurrencyDialog() }
        binding.rowWarningDays.setOnClickListener { showWarningDaysDialog() }
        binding.rowLogout.setOnClickListener { logout() }
    }

    private fun refreshThemeLabel() {
        binding.textThemeValue.text = getString(
            when (app.preferencesManager.themeMode) {
                ThemeMode.SYSTEM -> R.string.settings_theme_system
                ThemeMode.LIGHT -> R.string.settings_theme_light
                ThemeMode.DARK -> R.string.settings_theme_dark
            }
        )
    }

    private fun refreshCurrencyLabel() {
        binding.textCurrencyValue.text = app.preferencesManager.preferredCurrency
    }

    private fun refreshWarningDaysLabel() {
        binding.textWarningDaysValue.text = getString(R.string.settings_warning_days_value, app.preferencesManager.warningDaysBeforeDue)
    }

    private fun showThemeDialog() {
        val options = arrayOf(
            getString(R.string.settings_theme_system),
            getString(R.string.settings_theme_light),
            getString(R.string.settings_theme_dark)
        )
        val modes = arrayOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
        val current = modes.indexOf(app.preferencesManager.themeMode)

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_choose_theme)
            .setSingleChoiceItems(options, current) { dialog, which ->
                app.preferencesManager.themeMode = modes[which]
                refreshThemeLabel()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCurrencyDialog() {
        val currencies = BillCurrencies.codes.toTypedArray()
        val current = currencies.indexOf(app.preferencesManager.preferredCurrency).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_choose_currency)
            .setSingleChoiceItems(currencies, current) { dialog, which ->
                app.preferencesManager.preferredCurrency = currencies[which]
                refreshCurrencyLabel()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showWarningDaysDialog() {
        val options = arrayOf("1", "2", "3", "5", "7")
        val current = options.indexOf(app.preferencesManager.warningDaysBeforeDue.toString()).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_choose_warning_days)
            .setSingleChoiceItems(options, current) { dialog, which ->
                app.preferencesManager.warningDaysBeforeDue = options[which].toInt()
                refreshWarningDaysLabel()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun logout() {
        app.sessionManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
