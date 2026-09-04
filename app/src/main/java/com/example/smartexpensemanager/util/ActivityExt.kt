package com.example.smartexpensemanager.util

import android.app.Activity
import com.example.smartexpensemanager.SmartExpenseApp

val Activity.app: SmartExpenseApp
    get() = application as SmartExpenseApp
