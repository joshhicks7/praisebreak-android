package com.jagoalt.praisebreak.data

data class RuntimeState(
    val accumulatedMs: Long = 0,
    val segmentStartMs: Long = 0,
    val snoozeUntilMs: Long = 0,
    val breaksToday: Int = 0,
    val breaksDay: Int = 0
)
