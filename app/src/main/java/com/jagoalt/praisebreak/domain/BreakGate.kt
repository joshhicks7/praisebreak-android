package com.jagoalt.praisebreak.domain

import com.jagoalt.praisebreak.domain.model.PraiseSettings

data class WatchSlice(
    val watchedNow: Boolean,
    val accumulatedMs: Long,
    val snoozeUntilMs: Long,
    val breakOpen: Boolean
)

sealed interface GateDecision {
    data object Hold : GateDecision
    data object Open : GateDecision
}

object BreakGate {
    fun decide(nowMs: Long, settings: PraiseSettings, slice: WatchSlice): GateDecision {
        if (slice.breakOpen || !settings.enabled || !slice.watchedNow) return GateDecision.Hold
        if (nowMs < slice.snoozeUntilMs) return GateDecision.Hold
        val limit = settings.everyMinutes.coerceAtLeast(1) * 60_000L
        return if (slice.accumulatedMs >= limit) GateDecision.Open else GateDecision.Hold
    }
}
