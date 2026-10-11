package com.jagoalt.praisebreak.domain

/** Counts time in a watched app. Gaps longer than two minutes are clipped. */
class UsageClock {
    var accumulatedMs: Long = 0
        private set
    var segmentStartMs: Long = 0
        private set

    fun restore(accumulatedMs: Long, segmentStartMs: Long) {
        this.accumulatedMs = accumulatedMs.coerceAtLeast(0)
        this.segmentStartMs = segmentStartMs.coerceAtLeast(0)
    }

    fun setTracking(active: Boolean, nowMs: Long) {
        val already = segmentStartMs > 0
        if (active == already) return
        closeSegment(nowMs)
        segmentStartMs = if (active) nowMs else 0
    }

    fun elapsed(nowMs: Long): Long {
        return accumulatedMs + openGap(nowMs)
    }

    fun reset(stillTracking: Boolean, nowMs: Long) {
        accumulatedMs = 0
        segmentStartMs = if (stillTracking) nowMs else 0
    }

    private fun closeSegment(nowMs: Long) {
        accumulatedMs += openGap(nowMs)
        segmentStartMs = 0
    }

    private fun openGap(nowMs: Long): Long {
        if (segmentStartMs <= 0) return 0
        return (nowMs - segmentStartMs).coerceIn(0, MAX_GAP_MS)
    }

    companion object {
        const val MAX_GAP_MS = 2 * 60_000L
    }
}
