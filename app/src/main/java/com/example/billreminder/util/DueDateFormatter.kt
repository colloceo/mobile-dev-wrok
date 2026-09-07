package com.example.billreminder.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class Urgency { OVERDUE, DUE_SOON, UPCOMING }

object DueDateFormatter {

    private val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

    fun daysUntil(dueMillis: Long, now: Long = System.currentTimeMillis()): Int {
        val startOfToday = startOfDay(now)
        val startOfDue = startOfDay(dueMillis)
        val diffMillis = startOfDue - startOfToday
        return (diffMillis / (24L * 60 * 60 * 1000)).toInt()
    }

    fun urgencyFor(dueMillis: Long, warningDays: Int, now: Long = System.currentTimeMillis()): Urgency {
        val days = daysUntil(dueMillis, now)
        return when {
            days < 0 -> Urgency.OVERDUE
            days <= warningDays -> Urgency.DUE_SOON
            else -> Urgency.UPCOMING
        }
    }

    fun relativeLabel(dueMillis: Long, now: Long = System.currentTimeMillis()): String {
        val days = daysUntil(dueMillis, now)
        return when {
            days == 0 -> "Due today"
            days == 1 -> "Due tomorrow"
            days > 1 -> "Due in $days days"
            days == -1 -> "Overdue by 1 day"
            else -> "Overdue by ${abs(days)} days"
        }
    }

    fun formatted(dueMillis: Long): String = dateFormat.format(Date(dueMillis))

    private fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
