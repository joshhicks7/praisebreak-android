package com.jagoalt.praisebreak.domain

import java.util.Calendar
import java.util.TimeZone

object Schedule {
    fun allows(
        dayBit: Int,
        minuteOfDay: Int,
        days: Int,
        startMinute: Int,
        endMinute: Int
    ): Boolean {
        if (days and dayBit == 0) return false
        return minuteInside(minuteOfDay, startMinute, endMinute)
    }

    fun minuteInside(minute: Int, start: Int, end: Int): Boolean {
        if (start == end) return true
        return if (start < end) {
            minute in start until end
        } else {
            minute >= start || minute < end
        }
    }

    fun dayAndMinute(nowMs: Long, zone: TimeZone = TimeZone.getDefault()): Pair<Int, Int> {
        val calendar = Calendar.getInstance(zone)
        calendar.timeInMillis = nowMs
        val bit = 1 shl (calendar.get(Calendar.DAY_OF_WEEK) - 1)
        val minute = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        return bit to minute
    }

    fun dayKey(nowMs: Long, zone: TimeZone = TimeZone.getDefault()): Int {
        val calendar = Calendar.getInstance(zone)
        calendar.timeInMillis = nowMs
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return year * 10_000 + month * 100 + day
    }

    fun clockLabel(minuteOfDay: Int): String {
        val hour24 = minuteOfDay.coerceIn(0, 24 * 60 - 1) / 60
        val minute = minuteOfDay.coerceIn(0, 24 * 60 - 1) % 60
        val suffix = if (hour24 < 12) "AM" else "PM"
        val hour12 = when (val wrapped = hour24 % 12) {
            0 -> 12
            else -> wrapped
        }
        return "%d:%02d %s".format(hour12, minute, suffix)
    }
}
