package com.jagoalt.praisebreak.domain.model

data class BreakFace(
    val kind: BreakKind,
    val style: CoverStyle,
    val endsAtMs: Long,
    val durationMs: Long,
    val appLabel: String,
    val body: String,
    val reference: String,
    val loading: Boolean,
    val allowSnooze: Boolean
)
