package com.example.billreminder.util

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    var loggedInUserId: Long
        get() = prefs.getLong(KEY_USER_ID, NO_USER)
        set(value) = prefs.edit { putLong(KEY_USER_ID, value) }

    var loggedInUsername: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit { putString(KEY_USERNAME, value) }

    val isLoggedIn: Boolean
        get() = loggedInUserId != NO_USER

    fun login(userId: Long, username: String) {
        loggedInUserId = userId
        loggedInUsername = username
    }

    fun logout() {
        prefs.edit { clear() }
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        const val NO_USER = -1L
    }
}
