package com.jagoalt.praisebreak.domain.model

import java.util.Calendar

enum class Weekday(val calendarDay: Int, val initial: String, val nameShort: String) {
    Sun(Calendar.SUNDAY, "S", "Sun"),
    Mon(Calendar.MONDAY, "M", "Mon"),
    Tue(Calendar.TUESDAY, "T", "Tue"),
    Wed(Calendar.WEDNESDAY, "W", "Wed"),
    Thu(Calendar.THURSDAY, "T", "Thu"),
    Fri(Calendar.FRIDAY, "F", "Fri"),
    Sat(Calendar.SATURDAY, "S", "Sat");

    val bit: Int get() = 1 shl (calendarDay - 1)

    companion object {
        val all: Int = values().fold(0) { mask, day -> mask or day.bit }
    }
}
