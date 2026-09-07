package com.example.billreminder

import android.app.Application
import com.example.billreminder.data.local.AppDatabase
import com.example.billreminder.data.repository.AuthRepository
import com.example.billreminder.data.repository.BillRepository
import com.example.billreminder.data.repository.CurrencyRepository
import com.example.billreminder.data.repository.PaymentRepository
import com.example.billreminder.util.PreferencesManager
import com.example.billreminder.util.SessionManager

/**
 * Simple hand-rolled service locator: avoids pulling in a DI framework for
 * a coursework-scope app while still keeping repositories out of Activities.
 */
class BillReminderApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val sessionManager: SessionManager by lazy { SessionManager(this) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }

    val authRepository: AuthRepository by lazy { AuthRepository(database.userDao()) }
    val billRepository: BillRepository by lazy { BillRepository(database.billDao(), database.paymentDao()) }
    val paymentRepository: PaymentRepository by lazy { PaymentRepository(database.paymentDao()) }
    val currencyRepository: CurrencyRepository by lazy { CurrencyRepository() }

    override fun onCreate() {
        super.onCreate()
        preferencesManager.applyStoredThemeMode()
    }
}
