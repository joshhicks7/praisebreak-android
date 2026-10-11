package com.jagoalt.praisebreak

import com.jagoalt.praisebreak.domain.BreakGate
import com.jagoalt.praisebreak.domain.GateDecision
import com.jagoalt.praisebreak.domain.Schedule
import com.jagoalt.praisebreak.domain.UsageClock
import com.jagoalt.praisebreak.domain.WatchSlice
import com.jagoalt.praisebreak.domain.model.PraiseSettings
import com.jagoalt.praisebreak.domain.model.Weekday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BreakGateTest {
    private val settings = PraiseSettings(
        enabled = true,
        packages = setOf("com.example.app"),
        days = Weekday.all,
        startMinute = 0,
        endMinute = 0,
        everyMinutes = 20
    )

    @Test
    fun holdsUntilTheIntervalIsReached() {
        val early = WatchSlice(true, 19 * 60_000L, 0, false)
        val due = WatchSlice(true, 20 * 60_000L, 0, false)
        assertEquals(GateDecision.Hold, BreakGate.decide(0, settings, early))
        assertEquals(GateDecision.Open, BreakGate.decide(0, settings, due))
    }

    @Test
    fun holdsWhileSnoozedOrUnwatched() {
        val due = WatchSlice(true, 20 * 60_000L, 5_000, false)
        val away = WatchSlice(false, 20 * 60_000L, 0, false)
        assertEquals(GateDecision.Hold, BreakGate.decide(0, settings, due))
        assertEquals(GateDecision.Hold, BreakGate.decide(0, settings, away))
        assertEquals(GateDecision.Hold, BreakGate.decide(0, settings.copy(enabled = false), due.copy(snoozeUntilMs = 0)))
    }
}

class ScheduleTest {
    @Test
    fun daytimeWindow() {
        assertTrue(Schedule.minuteInside(10 * 60, 8 * 60, 22 * 60))
        assertFalse(Schedule.minuteInside(7 * 60, 8 * 60, 22 * 60))
    }

    @Test
    fun overnightWindow() {
        assertTrue(Schedule.minuteInside(23 * 60, 22 * 60, 6 * 60))
        assertTrue(Schedule.minuteInside(5 * 60, 22 * 60, 6 * 60))
        assertFalse(Schedule.minuteInside(12 * 60, 22 * 60, 6 * 60))
    }

    @Test
    fun equalBoundsMeanAllDay() {
        assertTrue(Schedule.allows(Weekday.Mon.bit, 15 * 60, Weekday.Mon.bit, 0, 0))
        assertFalse(Schedule.allows(Weekday.Sun.bit, 15 * 60, Weekday.Mon.bit, 0, 0))
    }
}

class UsageClockTest {
    @Test
    fun countsOnlyWhileTrackingAndClipsLongGaps() {
        val clock = UsageClock()
        clock.setTracking(true, 1_000)
        assertEquals(1_000L, clock.elapsed(2_000))
        clock.setTracking(false, 3_000)
        assertEquals(2_000L, clock.elapsed(9_000))
        clock.setTracking(true, 10_000)
        clock.setTracking(false, 10_000 + UsageClock.MAX_GAP_MS + 50_000)
        assertEquals(2_000L + UsageClock.MAX_GAP_MS, clock.accumulatedMs)
    }
}
