package com.example.smartexpensemanager

import android.app.Application
import com.example.smartexpensemanager.data.local.AppDatabase
import com.example.smartexpensemanager.data.repository.AuthRepository
import com.example.smartexpensemanager.data.repository.CategoryRepository
import com.example.smartexpensemanager.data.repository.ExchangeRateRepository
import com.example.smartexpensemanager.data.repository.TransactionRepository
import com.example.smartexpensemanager.util.PreferencesManager
import com.example.smartexpensemanager.util.SessionManager

/**
 * Simple hand-rolled service locator: avoids pulling in a DI framework for
 * a coursework-scope app while still keeping repositories out of Activities.
 */
class SmartExpenseApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val sessionManager: SessionManager by lazy { SessionManager(this) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }

    val authRepository: AuthRepository by lazy { AuthRepository(database.userDao()) }
    val transactionRepository: TransactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val categoryRepository: CategoryRepository by lazy { CategoryRepository(database.categoryDao()) }
    val exchangeRateRepository: ExchangeRateRepository by lazy { ExchangeRateRepository() }

    override fun onCreate() {
        super.onCreate()
        preferencesManager.applyStoredThemeMode()
    }
}
