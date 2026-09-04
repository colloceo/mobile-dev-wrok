package com.example.smartexpensemanager.data.mpesa

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Lightweight inbox for M-Pesa transactions detected by the notification
 * listener but not yet reviewed by the user. Backed by SharedPreferences
 * (JSON via Gson) rather than a Room table since this is a small, transient
 * queue, not durable financial data — items graduate into a real
 * TransactionEntity (via AddEditTransactionActivity) once the user assigns a
 * category and confirms.
 */
class PendingMpesaStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("pending_mpesa", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAll(): List<MpesaTransaction> {
        val json = prefs.getString(KEY_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<MpesaTransaction>>() {}.type
        return runCatching { gson.fromJson<List<MpesaTransaction>>(json, type) }.getOrNull() ?: emptyList()
    }

    fun add(transaction: MpesaTransaction) {
        val current = getAll()
        if (current.any { it.code == transaction.code }) return
        save(current + transaction)
    }

    fun remove(code: String) {
        save(getAll().filterNot { it.code == code })
    }

    private fun save(list: List<MpesaTransaction>) {
        prefs.edit { putString(KEY_LIST, gson.toJson(list)) }
    }

    companion object {
        private const val KEY_LIST = "pending_list"
    }
}
