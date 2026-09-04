package com.example.smartexpensemanager.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartexpensemanager.BuildConfig
import com.example.smartexpensemanager.R
import com.example.smartexpensemanager.databinding.ActivitySettingsBinding
import com.example.smartexpensemanager.ui.auth.LoginActivity
import com.example.smartexpensemanager.util.BottomNavHelper
import com.example.smartexpensemanager.util.NotificationAccessHelper
import com.example.smartexpensemanager.util.SupportedCurrencies
import com.example.smartexpensemanager.util.ThemeMode
import com.example.smartexpensemanager.util.TopLevelDestination
import com.example.smartexpensemanager.util.app

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

        binding.rowTheme.setOnClickListener { showThemeDialog() }
        binding.rowCurrency.setOnClickListener { showCurrencyDialog() }
        binding.rowLogout.setOnClickListener { logout() }
        binding.rowMpesa.setOnClickListener { NotificationAccessHelper.openSettings(this) }
    }

    override fun onResume() {
        super.onResume()
        refreshMpesaStatus()
    }

    private fun refreshMpesaStatus() {
        binding.textMpesaStatus.text = getString(
            if (NotificationAccessHelper.isEnabled(this)) R.string.settings_mpesa_enabled else R.string.settings_mpesa_disabled
        )
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
        binding.textCurrencyValue.text = app.preferencesManager.defaultCurrency
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
        val currencies = SupportedCurrencies.codes.toTypedArray()
        val current = currencies.indexOf(app.preferencesManager.defaultCurrency).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle(R.string.settings_choose_currency)
            .setSingleChoiceItems(currencies, current) { dialog, which ->
                app.preferencesManager.defaultCurrency = currencies[which]
                refreshCurrencyLabel()
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
