package com.example.billreminder.util

import android.app.Activity
import com.example.billreminder.BillReminderApp

val Activity.app: BillReminderApp
    get() = application as BillReminderApp
